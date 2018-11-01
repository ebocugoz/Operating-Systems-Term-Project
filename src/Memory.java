public class Memory {

	private int memorySize;
	private char[] memory;
	public int[] chunk;
	private int emptyIndex;

	public Memory(int size) {
		memorySize = size;
		memory = new char[size];
		chunk = new int[size];
		
		emptyIndex = 0;
	}

	void addInstructions(char[] buffer, int bufferSize, int BR)
	{
		for (int i = BR; i < bufferSize+BR; i++)
		{
			this.memory[i] = buffer[i - BR];
			this.chunk[i] = 1;
		}

		emptyIndex += bufferSize;
	}
	void removeInstructions(ProcessImage p)
	{
		int i = p.BR;
		while(i<p.LR)
		{
			
			this.chunk[i]=0;
			i++;
		}
	}


	char[]getInstruction(int PC, int BR)
	{
		char[]instruction = new char[4];
		instruction[0]=memory[PC+BR];
		instruction[1]=memory[PC+BR+1];
		instruction[2]=memory[PC+BR+2];
		instruction[3]=memory[PC+BR+3];

		return instruction;

	}

	int getEmptyIndex()
	{
		return this.emptyIndex;
	}

	public int getMemorySize() {
		return memorySize;
	}

}
