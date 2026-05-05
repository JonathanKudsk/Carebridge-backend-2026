package entitiesTest;

import com.carebridge.entities.Message;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MessageEntityTest {

    @Test
    public void emptyMessageShouldThrow() {
        Message message = new Message();
        assertThrows(IllegalArgumentException.class, () -> message.setMessage(""));
    }

    @Test
    public void nullMessageShouldThrow() {
        Message message = new Message();
        assertThrows(IllegalArgumentException.class, () -> message.setMessage(null));
    }

    @Test
    public void fiveHundredMessageShouldBeAllowed() {
        Message message = new Message();
        String fiveHundred = "a".repeat(500);
        message.setMessage(fiveHundred);
        assertEquals(500, message.getMessage().length());
    }

    @Test
    public void twoThousandMessageShouldBeAllowed() {
        Message message = new Message();
        String twoThousand = "a".repeat(2000);
        message.setMessage(twoThousand);
        assertEquals(2000, message.getMessage().length());
    }

    @Test
    public void overTwoThousandMessageShouldNotBeAllowed() {
        Message message = new Message();
        String tooLong = "a".repeat(2001);
        assertTrue(tooLong.length() > 2000);
        assertThrows(IllegalArgumentException.class, () -> message.setMessage(tooLong));
    }

    @Test
    public void singleCharacterMessageShouldBeAllowed() {
        Message message = new Message();
        message.setMessage("a");
        assertEquals(1, message.getMessage().length());
    }
    @Test
    public void whitespaceOnlyMessageShouldThrow() {
        Message message = new Message();
        assertThrows(IllegalArgumentException.class, () -> message.setMessage("     "));
    }
}
