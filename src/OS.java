import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class OS extends Thread {

	private final int QUANTUM = 5;

	private CPU cpu;
	private Memory memory;
	private volatile List<ProcessImage> readyQueue;
	private volatile List<ProcessImage> blockedQueue;
	private volatile List<ProcessImage> fileInputQueue;
	private volatile List<Integer> consoleInputQueue;
	private volatile List<char[]> MemoryQueue;
	private volatile List<Integer> instSize;
	private Semaphore mutex;
	private Semaphore mutexFile;
	private Semaphore mutexConsole;
	private InputThread inputThread;
	private String fileName;
	public boolean isRun = true;
	
	public int findFirstFit(int size)
	{
		if(size<0)
			return -1;
		
		
		System.out.println("searching for a fit");
		for(int i=0;i<memory.getMemorySize();i++)
		{
			int index = -1;
			int counter =0;
			if( memory.chunk[i]==0)
			{
				 index = i;
				while(i<memory.getMemorySize() &&memory.chunk[i]==0)
				{
					counter++;
					i++;
				}
			}
			if(counter>=size)
				{
				System.out.println("Find a fit with index "+index+"and size is "+size);
				return index;
				}
			else
			{
				counter = 0;
				index = -1;
			}
		}
		System.out.println("No fit");
		return -1;
	}
	
	private class consoleProducer extends Thread {
		
		
		@Override
		public void run()
		{
			System.out.println("Console input thread started");
			while(isRun)
			{
				try {
					mutexConsole.acquire();
					int sizeQ = consoleInputQueue.size();
					mutexConsole.release();
					while(sizeQ == 5)
					{
						sleep(100);
						mutexConsole.acquire();
						sizeQ = consoleInputQueue.size();
						mutexConsole.release();
					}
					mutexConsole.acquire();
					System.out.println("GETTING  CONSOLE INPUT ");
					Scanner in = new Scanner(System.in); 
					int i = in.nextInt();
					in.close();
					mutexConsole.release();
					
					mutexConsole.acquire();
					consoleInputQueue.add(i);
					mutexConsole.release();
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private class consoleConsumer extends Thread{
		@Override 
		public void run()
		{
			while(isRun)
			{
				
			
			try {
				mutexConsole.acquire();
				boolean isEmptyCONSOLE = consoleInputQueue.isEmpty();
				mutexConsole.release();
				while(isEmptyCONSOLE)
				{
					sleep(2000);
					
					mutexConsole.acquire();
					isEmptyCONSOLE = consoleInputQueue.isEmpty();
					mutexConsole.release();
				}
				
				mutexConsole.acquire();
				int i = consoleInputQueue.get(0);
				consoleInputQueue.remove(0);
				mutexConsole.release();
				System.out.println("Console Consumer got the value: "+i);
				mutex.acquire();
				boolean isEmpty = blockedQueue.isEmpty();
				mutex.release();
				if(isEmpty)
				{
					System.out.println("Blocked Queue is empty now I will sleep and try again with the same value: "+i);
				}
				while(isEmpty)
				{
					sleep(2000);
					
					mutex.acquire();
					isEmpty = blockedQueue.isEmpty();
					mutex.release();
				}
				System.out.println("There is something in the blocked queue set its V as : "+i);
				
				mutex.acquire();
				ProcessImage p = blockedQueue.get(0);
				p.V = i;
				blockedQueue.remove(0);
				System.out.println("Adding to ready queue");
				readyQueue.add(p);
				mutex.release();
				
				
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
		}
	}
	private class fileInputThreadProducer extends Thread {
		
	
		
		
		public void load(String inputfile) throws IOException, InterruptedException
		{
			BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(inputfile), StandardCharsets.UTF_8));
			
			String line="";
			while((line = br.readLine()) != null && line.trim().isEmpty()==false) {
				mutexFile.acquire();
				int Qsize = fileInputQueue.size();
				mutexFile.release();
				while(Qsize==5)
				{
					try {
						sleep(100);
						mutexFile.acquire();
						 Qsize = fileInputQueue.size();
						mutexFile.release();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
					
					
					
				String[] parts = line.split(" ");
				System.out.println(parts[0]);
				String fileName = parts[0]; 
				String waitTime = parts[1];
				int millis =  Integer.parseInt(waitTime);
				Assembler assembler = new Assembler();
				loadProcess(fileName.substring(0, fileName.length()-4),assembler);
				try {
					System.out.println("Sleeping for "+millis);
					sleep(millis);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
		}
		public void loadProcess(String processFile,Assembler assembler)
		{
			try {
				System.out.println( "Creating binary file for "+ processFile+"...") ;
				int instructionSize = assembler.createBinaryFile(processFile+".asm", processFile+".bin");
				char[] process = assembler.readBinaryFile(instructionSize, processFile+".bin");
				MemoryQueue.add(process);
				instSize.add(instructionSize);
				

				mutexFile.acquire();
				System.out.println( "Adding to file input queue ") ;
				fileInputQueue.add(new ProcessImage(processFile,0,instructionSize)); // readyqueue ya ekledi.

				mutexFile.release();

				
				System.out.println("Process is loaded !");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run()
		{
			try {
				System.out.println("File input prodcuer has started and reading input files name from "+fileName);
				try {
					load(fileName);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

private class fileInputThreadConsumer extends Thread {
	
		@Override
		public void run()
		{
			System.out.println("File input consumer has started ");
			boolean isEmpty;
			
			
			while(isRun)
			{
				
				try {
					mutexFile.acquire();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				isEmpty = fileInputQueue.isEmpty();
				mutexFile.release();
				
				if(!isEmpty)
				{
					try {
						mutex.acquire();
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						mutexFile.acquire();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
					
					int processSize  = fileInputQueue.get(0).LR-fileInputQueue.get(0).BR;
					int index = findFirstFit(processSize);
					mutex.release();
					mutexFile.release();
					if(index!=-1)
					{
						System.out.println("There is enough space in the memory adding to ready queue");
						try {
							mutex.acquire();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							mutexFile.acquire();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						ProcessImage addImage = fileInputQueue.get(0);
						fileInputQueue.remove(0);
						addImage.BR = index;
						addImage.LR += addImage.BR;
						readyQueue.add(addImage);
						System.out.println("Added to ready Queue");
						System.out.println("Loading process to memory...");
						char[] process = MemoryQueue.get(0);
						MemoryQueue.remove(0);
						int instructionSize = instSize.get(0);
						System.out.println("SIZE: "+instructionSize);
						instSize.remove(0);
						
						memory.addInstructions(process, instructionSize, memory.getEmptyIndex()); // memory e koydu.
						mutex.release();
						mutexFile.release();
						
					}
					else
					{
						try {
							this.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
				
			
				
			}
		}
		
	}
	public OS(int size,String file) {
		this.memory = new Memory(size);
		this.cpu = new CPU(memory);
		this.mutex=new Semaphore(1);
		this.mutexFile=new Semaphore(1);
		this.mutexConsole=new Semaphore(1);
		this.readyQueue = new ArrayList<ProcessImage>();
		this.blockedQueue = new ArrayList<ProcessImage>();
		this.consoleInputQueue = new ArrayList<Integer>();
		this.fileInputQueue = new ArrayList<ProcessImage>();
		this.MemoryQueue = new ArrayList<char[]>();
		this.instSize = new ArrayList<Integer>();
		this.fileName = file;
		this.inputThread = new InputThread(mutex, blockedQueue, readyQueue,mutexConsole,mutexFile);
		inputThread.start();
		fileInputThreadConsumer fct = new fileInputThreadConsumer();
		fileInputThreadProducer fpt = new fileInputThreadProducer();
		consoleProducer cpt = new consoleProducer();
		consoleConsumer cct = new consoleConsumer();
		
		
		fpt.start();
		fct.start();
		cpt.start();
		cct.start();
	}
	

	
	

	@Override
	public void run() {
		
		try {
			
			
			
			while (true) {

				mutex.acquire();
				boolean isBlockedQueueEmpty = blockedQueue.isEmpty();
				boolean isReadyQueueEmpty = readyQueue.isEmpty();
				mutex.release();
				if(isBlockedQueueEmpty && isReadyQueueEmpty) {
					
					break;
				}

				if (!isReadyQueueEmpty) {
					System.out.println("Executing " + (readyQueue.get(0)).processName);
					cpu.transferFromImage(readyQueue.get(0));
					for (int i = 0; i < QUANTUM; i++) {
						
						if (cpu.getPC() < cpu.getLR()) {
							cpu.fetch(); 
							int returnCode = cpu.decodeExecute();

							if (returnCode == 0)  {
								System.out.println("Process " + readyQueue.get(0).processName + " made a system call for ");
								if (cpu.getV() == 0) {
									System.out.println( "Input, transfering to blocked queue and waiting for input...");
									ProcessImage p=new ProcessImage();
									this.cpu.transferToImage(p);
									
									mutex.acquire();
									readyQueue.remove(0);
									blockedQueue.add(p);
									mutex.release();
								} 
								else { //syscall for output
									System.out.print("Output Value: ");
									ProcessImage p=new ProcessImage();
									cpu.transferToImage(p);

									mutex.acquire();
									readyQueue.remove(0);
									System.out.println( p.V +"\n");
									readyQueue.add(p);
									mutex.release();
								}
								//Process blocked, need to end quantum prematurely
								break;
							}
						}
						else {
							System.out.println("Process " + readyQueue.get(0).processName +" has been finished! Removing from the queue...\n" );
							ProcessImage p = new ProcessImage();
							cpu.transferToImage(p);
							p.writeToDumpFile();
							System.out.println("Now deleting process from the memory ");
							memory.removeInstructions(p);

							mutex.acquire();
							readyQueue.remove(0);
							mutex.release();
							break;
						}

						if (i == QUANTUM - 1) {
							//quantum finished put the process at the end of readyQ
							System.out.println ("Context Switch! Allocated quantum have been reached, switching to next process...\n");
							ProcessImage p = new ProcessImage();
							cpu.transferToImage(p);  

							mutex.acquire();
							readyQueue.remove(0);
							readyQueue.add(p);
							mutex.release();
						}
					}
				}
			}
			isRun = false;
			inputThread.stopThread();
			System.out.println("Execution of all processes has finished!");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
