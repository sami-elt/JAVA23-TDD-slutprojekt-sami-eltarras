import javax.naming.InsufficientResourcesException;

public class ATM {
    private User currentUser;

    private BankInterFace bankInterFace;

    private int tries;

    public ATM(BankInterFace bankInterFace) {
        this.bankInterFace = bankInterFace;
        this.currentUser = null;
    }

    public boolean insertCard(String userId) {
        currentUser = bankInterFace.getUserById(userId);

        if (isCardLocked(userId)){
            return false;
        }

        return currentUser != null;
    }


    public boolean enterPin(String pin, String userId) {

        if (isCardLocked(userId)){
            throw new RuntimeException("card is locked");
        }

        if (bankInterFace.isPinValid(userId, pin)){
            currentUser.resetFailedAttempts();
            return true;
        } else {
            currentUser.incrementFailedAttempts();

            tries = currentUser.getFailedAttempts();

            if (tries > 2) {
                currentUser.lockCard();
            }

            return false;
        }

    }

    private boolean isCardLocked(String userId){
        return bankInterFace.isCardLocked(userId);
    }

        public static String getBankName() {
        return "MockBank";
    }


    public double checkBalance() {
        return currentUser.getBalance();
    }

    public void deposit(double amount) {
        if (amount > 0){
            currentUser.deposit(amount);
        } else {
            throw new RuntimeException("Insufficient amount");
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && currentUser.getBalance() >= amount){
            currentUser.withdraw(amount);
            return true;
        } else {
            throw new RuntimeException("Insufficient amount");
        }
    }

    public void endSession(){
        currentUser = null;
    }


}
