package dao;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.impl.ChatRoomDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.ChatRoom;
import com.carebridge.entities.ChatRoomUser;
import com.carebridge.entities.User;
import com.carebridge.entities.enums.Role;
import com.carebridge.exceptions.ApiRuntimeException;
import org.junit.Assert;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ChatRoomDAOTest {

    private ChatRoomDAO chatRoomDAO;
    private UserDAO userDAO;

    private List<ChatRoom> testChatRooms;
    private User testUser;
    private List<User> extraUsers = new ArrayList<>();

    @BeforeAll
    public void setupClass() {
        HibernateConfig.setTest(true);
        HibernateConfig.getEntityManagerFactoryForTest();
        chatRoomDAO = ChatRoomDAO.getInstance();
        userDAO = UserDAO.getInstance();
    }

    @BeforeEach
    public void setup() {
        extraUsers.clear();
        // Create a test user for chat room membership
        testUser = new User();
        testUser.setName("Chat Room Test User");
        testUser.setEmail("chatroomtestuser@example.com");
        testUser.setRole(Role.USER);
        testUser.setPassword("test123");
        testUser.setIsEmployed(true);
        userDAO.create(testUser);

        // Create multiple chat rooms for pagination testing
        testChatRooms = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            ChatRoom room = new ChatRoom();
            ChatRoomUser member = new ChatRoomUser();
            member.setUser(testUser);
            room.setChatRoomUser(List.of(member));
            ChatRoom created = chatRoomDAO.create(room);
            testChatRooms.add(created);
        }
    }

    @AfterEach
    public void cleanup() {
        // Delete chat rooms FIRST
        if (testChatRooms != null) {
            for (ChatRoom room : testChatRooms) {
                if (room != null) {
                    try {
                        chatRoomDAO.delete(room.getId());
                    } catch (Exception ignored) {
                    }
                }
            }
            testChatRooms.clear();
        }

        // Delete extra users created in tests
        for (User user : extraUsers) {
            if (user != null) {
                try {
                    userDAO.delete(user.getId());
                } catch (Exception ignored) {
                }
            }
        }
        extraUsers.clear();

        // Delete test user last
        if (testUser != null) {
            try {
                userDAO.delete(testUser.getId());
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    public void testReadAllPagedFullPage() {
        List<ChatRoom> page = chatRoomDAO.readAllPaged(0, 5);
        assertEquals(5, page.size());
        assertNotNull(page.getFirst().getId());
        assertNotNull(page.getFirst().getChatRoomUser());
    }

    @Test
    public void testReadAllPagedPartialPage() {
        // Skip first 5, get remaining 2
        List<ChatRoom> partialPage = chatRoomDAO.readAllPaged(1, 5);
        assertEquals(2, partialPage.size());
        assertNotNull(partialPage.getFirst().getId());
    }

    @Test
    public void testReadAllPagedWithPageSizeOne() {
        List<ChatRoom> singlePage = chatRoomDAO.readAllPaged(0, 1);
        assertEquals(1, singlePage.size());
        assertNotNull(singlePage.getFirst().getId());
    }

    @Test
    public void testEvaluateAndUpdateChatRoomStatus() {
        // Create a second user with isEmployed = true
        User employedUser = new User();
        employedUser.setName("Employed User");
        employedUser.setEmail("employed@example.com");
        employedUser.setRole(Role.USER);
        employedUser.setPassword("test123");
        employedUser.setIsEmployed(false);
        userDAO.create(employedUser);
        extraUsers.add(employedUser);  // Track for cleanup

        // Create a chat room with both users
        ChatRoom room = new ChatRoom();
        ChatRoomUser member1 = new ChatRoomUser();
        member1.setUser(testUser);

        ChatRoomUser member2 = new ChatRoomUser();
        member2.setUser(employedUser);

        room.setChatRoomUser(List.of(member1, member2));
        room = chatRoomDAO.create(room);
        testChatRooms.add(room);  // Track for cleanup

        // Room should be active initially
        assertTrue(room.isActive());

        // Evaluate the status - with 1 employed and 2 total, should deactivate
        chatRoomDAO.evaluateAndUpdateChatRoomStatus(room.getId());
        ChatRoom updatedRoom = chatRoomDAO.read(room.getId());
        assertFalse(updatedRoom.isActive());
    }
}
