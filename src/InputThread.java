import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class InputThread extends Thread {

	private volatile List<ProcessImage> blockedQueue;
	private volatile List<ProcessImage> readyQueue;

	private Semaphore mutex;
	private Semaphore mutexConsole;
	private Semaphore mutexFile;

	private volatile boolean isRunning;

	public InputThread(Semaphore mtx, List<ProcessImage> blockedQ, List<ProcessImage> readyQ,Semaphore mtxC,Semaphore mtxF) {
		this.mutex = mtx;
		this.blockedQueue = blockedQ;
		this.readyQueue = readyQ;
		this.mutexConsole = mtxC;
		this.mutexFile = mtxF;
	}

	@Override
	public void run(){
		isRunning = true;
		try {
			while (isRunning) {
				mutex.acquire();
				mutexConsole.acquire();
				mutexFile.acquire();
				boolean isBlockedQueueEmpty = blockedQueue.isEmpty();
				mutex.release();
				mutexConsole.release();
				mutexFile.release();

				if (!isBlockedQueueEmpty) {
					Scanner in = new Scanner(System.in); 
					int i = in.nextInt();
					in.close();
					
					mutex.acquire();
					mutexConsole.acquire();
					mutexFile.acquire();
					ProcessImage p = blockedQueue.get(0);
					blockedQueue.remove(0);
					p.V = i;
					readyQueue.add(p);
					mutex.release();
					mutexConsole.release();
					mutexFile.release();
					
				}
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	public void stopThread() {
		isRunning = false;
	}
}
