import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ATMTest {

    private BankInterFace mockBankInterFace;
    private ATM atm;
    private User mockUser;


    @BeforeEach
    void setUp(){
        mockBankInterFace = mock(BankInterFace.class);
        mockUser = mock(User.class);
        atm = new ATM(mockBankInterFace);
    }

    @Test
    @DisplayName("Get user information from bank with correct id")
    void getUserInfoWithCard(){
        when(mockBankInterFace.getUserById("123")).thenReturn(mockUser);

        assertTrue(atm.insertCard("123"));
        assertFalse(atm.insertCard(null));
    }

    @Test
    @DisplayName("test locked user")
    void cardIsLocked(){
        when(mockBankInterFace.getUserById("123")).thenReturn(mockUser);
        when(mockBankInterFace.isCardLocked("123")).thenReturn(true);

        assertFalse(atm.insertCard("123"));
    }

    @Test
    @DisplayName("Testing login with pincode")
    void loginWithPin(){
        when(mockBankInterFace.getUserById("123")).thenReturn(mockUser);
        when(mockBankInterFace.isPinValid("123","1111")).thenReturn(true);

        atm.insertCard("123");
       assertTrue(atm.enterPin("1111","123"));
       assertFalse(atm.enterPin("1234","123"));

    }

    @Test
    @DisplayName("test if locked after 3 failed attempts")
    void lockedCardAfterTries(){
        when(mockBankInterFace.getUserById("123")).thenReturn(mockUser);
        when(mockBankInterFace.isPinValid("123", "3333")).thenReturn(false); // Felaktig PIN

        when(mockUser.getFailedAttempts())
                .thenReturn(1)
                .thenReturn(2)
                .thenReturn(3);

        atm.insertCard("123");

        atm.enterPin("3333", "123");
        atm.enterPin("3333", "123");
        atm.enterPin("3333", "123");

        verify(mockUser).lockCard();

    }

    @Test
    @DisplayName("check for balance")
    void balanceCheck(){
        when(mockBankInterFace.getUserById("123")).thenReturn(mockUser);
        when(mockUser.getBalance()).thenReturn(100.0);

        atm.insertCard("123");
        assertEquals(100.0, atm.checkBalance(), "borde vara 100");
    }

    @Test
    @DisplayName("check valid and invalid desposit")
    void deposits(){
        when(mockBankInterFace.getUserById("123")).thenReturn(mockUser);
        when(mockUser.getBalance()).thenReturn(200.0);
        atm.insertCard("123");
        atm.deposit(100.0);

        assertAll("check deposits",
                () ->  verify(mockUser).deposit(100.0),
                () ->  assertThrows(RuntimeException.class, () -> atm.deposit(-10.0))
        );
    }


    @Test
    @DisplayName("check withdraw")
    void withDraws(){
        when(mockBankInterFace.getUserById("123")).thenReturn(mockUser);
        when(mockUser.getBalance()).thenReturn(200.0);

        atm.insertCard("123");
        atm.withdraw(100.0);

        assertAll("check withdraws",
                () -> verify(mockUser, times(1)).getBalance(),
                () -> verify(mockUser, times(1)).withdraw(100.0),
                () -> assertThrows(RuntimeException.class, () -> atm.withdraw(300.0))
                );
    }

    @Test
    @DisplayName("test static bankname")
    void bankStaticName() {
        assertEquals("MockBank", ATM.getBankName());

        try (MockedStatic<ATM> mockedATM = mockStatic(ATM.class)) {
            mockedATM.when(ATM::getBankName).thenReturn("notMockBank");
            assertEquals("notMockBank", ATM.getBankName());
        }

        assertEquals("MockBank", ATM.getBankName());
    }

    @Test
    @DisplayName("test end of session")
    void endSession(){
        when(mockBankInterFace.getUserById("123")).thenReturn(mockUser);

        atm.insertCard("123");
        atm.endSession();

        assertFalse(atm.insertCard(null), "false om det inte finns något kort");
    }
}