package eventManageSystem;

public class InvalidTicketAmountException extends RuntimeException {
	public InvalidTicketAmountException(String message) {
		super(message);
	}
}