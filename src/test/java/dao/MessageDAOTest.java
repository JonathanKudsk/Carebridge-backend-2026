package dao;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.impl.ChatRoomDAO;
import com.carebridge.dao.impl.MessageDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.ChatRoom;
import com.carebridge.entities.Message;
import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
import org.junit.jupiter.api.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MessageDAOTest {

    private MessageDAO messageDAO;
    private UserDAO userDAO;
    private ChatRoomDAO chatRoomDAO;

    private User testUser;
    private ChatRoom testChatRoom;
    private Message testMessage;

    @BeforeAll
    public void setupClass() {
        HibernateConfig.setTest(true);
        HibernateConfig.getEntityManagerFactoryForTest();
        messageDAO = MessageDAO.getInstance();
        userDAO = UserDAO.getInstance();
        chatRoomDAO = ChatRoomDAO.getInstance();
    }

    @BeforeEach
    public void setup() {
        // Each test gets a fresh user, room, and message so the assertions stay isolated.
        testUser = new User();
        testUser.setName("Message Test User");
        testUser.setEmail("messagetestuser@example.com");
        testUser.setRole(Role.USER);
        testUser.setPassword("test123");
        userDAO.create(testUser);

        testChatRoom = new ChatRoom();
        testChatRoom = chatRoomDAO.create(testChatRoom);

        testMessage = new Message();
        testMessage.setUser(testUser);
        testMessage.setChatRoom(testChatRoom);
        testMessage.setMessage("Hello World!");
        testMessage.setTimestamp(Timestamp.from(Instant.now().plusSeconds(60)));
        testMessage = messageDAO.create(testMessage);
    }

    @AfterEach
    public void cleanup() {
        if (testMessage != null) {
            try {
                messageDAO.delete(testMessage.getId());
            } catch (Exception ignored) {}
        }
        if (testChatRoom != null) {
            try {
                chatRoomDAO.delete(testChatRoom.getId());
            } catch (Exception ignored) {}
        }
        if (testUser != null) {
            try {
                userDAO.delete(testUser.getId());
            } catch (Exception ignored) {}
        }
    }

    @Test
    public void testCreateMessage() {
        Message message = new Message();
        message.setUser(testUser);
        message.setChatRoom(testChatRoom);
        message.setMessage("Create Message");
        message.setTimestamp(Timestamp.from(Instant.now().plusSeconds(120)));

        Message created = messageDAO.create(message);
        assertTrue(created.getId() > 0);
        assertEquals("Create Message", created.getMessage());
        assertNotNull(created.getTimestamp());

        messageDAO.delete(created.getId());
    }

    @Test
    public void testReadMessage() {
        Message read = messageDAO.read(testMessage.getId());
        assertNotNull(read);
        assertEquals("Hello World!", read.getMessage());
        assertEquals(testUser.getId(), read.getUser().getId());
        assertEquals(testChatRoom.getId(), read.getChatRoom().getId());
    }

    @Test
    public void testUpdateMessage() {
        testMessage.setMessage("Updated Hello World!");
        Message updated = messageDAO.update(testMessage.getId(), testMessage);
        assertEquals("Updated Hello World!", updated.getMessage());
    }

    @Test
    public void testReadByChatRoom() {
        Message second = new Message();
        second.setUser(testUser);
        second.setChatRoom(testChatRoom);
        second.setMessage("Second Hello World!");
        second.setTimestamp(Timestamp.from(Instant.now().plusSeconds(180)));
        second = messageDAO.create(second);

        List<Message> messages = messageDAO.readByChatRoom(testChatRoom.getId());
        assertTrue(messages.size() >= 2);
        assertEquals(testChatRoom.getId(), messages.getFirst().getChatRoom().getId());
        assertTrue(messages.stream().anyMatch(m -> "Hello World!".equals(m.getMessage())));
        assertTrue(messages.stream().anyMatch(m -> "Second Hello World!".equals(m.getMessage())));

        messageDAO.delete(second.getId());
    }

    @Test
    public void testDeleteMessage() {
        Message message = new Message();
        message.setUser(testUser);
        message.setChatRoom(testChatRoom);
        message.setMessage("Delete Message");
        message.setTimestamp(Timestamp.from(Instant.now().plusSeconds(240)));

        Message created = messageDAO.create(message);
        messageDAO.delete(created.getId());
        assertNull(messageDAO.read(created.getId()));
    }
}



