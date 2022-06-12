package bgu.spl.mics.application.objects;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.services.GPUDataService;

import javax.swing.text.StyledEditorKit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {
	private Collection<MicroService> GPUDataService;
	private ConcurrentLinkedDeque<GPU> GPUS;
	private ConcurrentLinkedDeque<CPU> CPUS;



	private LinkedBlockingQueue<LinkedBlockingQueue<DataBatch>> dataQ;
	private Object dataQLock = new Object();
	private ConcurrentHashMap<MicroService, LinkedBlockingQueue<DataBatch>> getDataQ;
	private AtomicBoolean gpuKiller;
	private AtomicBoolean cpuKiller;

	//Statistics
	private ConcurrentLinkedDeque trainedModelsNames;
	private int totalDataBatchProcessed;
	private int cpuTimeUnitUsed;
	private int gpuTimeUnitUsed;

	//MsgBus singleton
	private static Cluster instance = null;
	private static boolean isDone = false;


	//Should only occur once (singleton)
	private Cluster() {
		GPUDataService = new ConcurrentLinkedDeque<>();
		GPUS = new ConcurrentLinkedDeque<>();
		CPUS = new ConcurrentLinkedDeque<>();
		dataQ = new LinkedBlockingQueue<>();
		getDataQ = new ConcurrentHashMap<>();
		trainedModelsNames = new ConcurrentLinkedDeque();
		totalDataBatchProcessed = 0;
		cpuTimeUnitUsed = 0;
		gpuTimeUnitUsed = 0;
		gpuKiller = new AtomicBoolean(false);
		cpuKiller = new AtomicBoolean(false);
	}
	/**
	 * Retrieves the single instance of this class.
	 */
	public static Cluster getInstance() {
		if (!isDone) {
			synchronized (Cluster.class) {
				if (!isDone) {
					instance = new Cluster();
					isDone = true;
				}
			}
		}
		return instance;
	}


	public ConcurrentHashMap<MicroService, LinkedBlockingQueue<DataBatch>> getGetDataQ(){
		return getDataQ;
	}
	public LinkedBlockingQueue<LinkedBlockingQueue<DataBatch>> getDataQueue() {
		return dataQ;
	}
	public void gpuRegister(MicroService m) { //the gpu DataService registers itself to the getDataQ;
		getDataQ.put(m, new LinkedBlockingQueue<>());
		GPUDataService.add(m);
		GPUS.add(((GPUDataService)m).getGpu());
		//delete if needed
//		if (m.getClass().equals(GPUDataService.class)){
//			GPUDataService.add(m);
//			GPUS.add(((GPUDataService)m).getGpu());
//		}
	}


	public void cpuRegister(CPU cpu){
		CPUS.add(cpu);
	}


	public void terminateDataServices(){
	//find a better way to kill the services
		if (gpuKiller.compareAndSet(false, true)){
			//free gpus waiting for data
			for (MicroService g: GPUDataService){
				getDataQ.get(g).add(new DataBatch(null, 0));
			}
																					//gpuKiller.set(true);
		}

		if (cpuKiller.compareAndSet(false, true)){
																					//cpuKiller.set(true);
			//free cpus waiting for data
			LinkedBlockingQueue<DataBatch> cpuDummyBatches = new LinkedBlockingQueue<>() ;
			for (CPU c: CPUS){
				cpuDummyBatches.add(new DataBatch(null, 0));
			}
			dataQ.add(cpuDummyBatches);
		}
	}

	public boolean getCpuKiller(){
		return cpuKiller.get();
	}

	public DataBatch cpuAwaitData() {
		DataBatch temp = null;
		//only one cpu waits for data at a time
		synchronized (dataQLock){
			LinkedBlockingQueue<DataBatch> tempC = null;
			//take dataBatch in round-robing style. if no more data left to process discard empty databatch queue.
			try {
				tempC = dataQ.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				temp = tempC.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (!tempC.isEmpty()){
				dataQ.add(tempC);
			}
		}
		return temp;
	}

	//gpu waits for data to be input into his queue. blocking method.
	public DataBatch gpuAwaitData(MicroService m){
		LinkedBlockingQueue<DataBatch> tempQ = getDataQ.get(m);
		DataBatch output = null;
		try {
			output = tempQ.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return output;
	}



	//adds the Collection of Databatches from the gpu to the queue the cpus can take from.
	public void gpuPushDataCollection(LinkedBlockingQueue<DataBatch> dataBatchQueue) {
		dataQ.add(dataBatchQueue);

	}

	//push processed data into the gpus queue so he can take data from the cpus.
	public void cpuPushData(DataBatch dataBatch){
		MicroService dest = dataBatch.getData().getDataService();
		Queue<DataBatch> destQ = getDataQ.get(dest);
		destQ.add(dataBatch);

	} //cpu pushes a DataBatch into the correct gpu Q

	public boolean gpuQIsEmpty(GPUDataService m){
		return (getDataQ.get(m).isEmpty());
	}
}
