package com.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Assume;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.statistics.Statistics;

public class UserTest {
    private UserSerdes serdes;
    
    public UserTest() {
        serdes = new UserSerdes();
    }
    
    @Provide
    Arbitrary<String> usernames() {
        return Arbitraries.strings().alpha().ofMaxLength(64);
    }

    @Provide
    Arbitrary<User> users() {
        final Arbitrary<OffsetDateTime> dates = Arbitraries
            .of(List.copyOf(ZoneId.getAvailableZoneIds()))
            .flatMap(zone ->  Arbitraries
                .longs()
                .between(1266258398000L, 1897410427000L) /* ~ +/- 10 years */
                .unique()
                .map(epochMilli -> Instant.ofEpochMilli(epochMilli))
                .map(instant -> OffsetDateTime.from(instant.atZone(ZoneId.of(zone)))));

        return Combinators
            .combine(usernames(), dates)
            .as((username, created) -> new User(username).created(created));
    }

    @Property
    void serdes(@ForAll("users") User user) {
        final String json = serdes.serialize(user);

        assertThat(serdes.deserialize(json))
            .satisfies(other -> {
                assertThat(user.getUsername()).isEqualTo(other.getUsername());
                assertThat(user.getCreated().isEqual(other.getCreated())).isTrue();
            });
        
        Statistics.collect(user.getCreated().getOffset());
    }
    
    @Property
    void equals(@ForAll("usernames") String username, @ForAll("usernames") String other) {
        Assume.that(!username.equals(other));
        
        assertThat(new User(username))
            .isEqualTo(new User(username))
            .isNotEqualTo(new User(other))
            .extracting(User::hashCode)
            .isEqualTo(new User(username).hashCode());
    }
}
