public interface BankInterFace {

     User getUserById(String id);

     boolean isPinValid(String id, String pin);
     boolean isCardLocked(String id);
}
