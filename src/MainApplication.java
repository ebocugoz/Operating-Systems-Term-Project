import java.util.List;

public class MainApplication {
	

	
	

	public static void main(String[] args) {
		Assembler assembler = new Assembler();
		OS os = new OS(100,"inputSequence.txt");
		

		os.start();
	}
}
