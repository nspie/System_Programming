package bgu.spl.mics.application;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.services.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

/**
 * This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    public static void main(String[] args) {

        Cluster cluster = Cluster.getInstance();
        int totalDataBatchProcessed = 0;
        int cpuTimeUnitUsed = 0;
        int gpuTimeUnitUsed = 0;

        ArrayList<Student> students = new ArrayList<>();
        ArrayList<Model> studentModels;
        ArrayList<StudentService> studentsServices = new ArrayList<>();

        ArrayList<GPU> gpus = new ArrayList<>();
        ArrayList<GPUService> gpuServices = new ArrayList<>();
        ArrayList<GPUDataService> gpuDataServices = new ArrayList<>();

        ArrayList<CPU> cpus = new ArrayList<>();
        ArrayList<CPUService> cpuServices = new ArrayList<>();
        ArrayList<CPUDataService> cpuDataServices = new ArrayList<>();

        ArrayList<ConfrenceInformation> conferences = new ArrayList<>();
        ArrayList<ConferenceService> conferencesServices = new ArrayList<>();

        ArrayList<Thread> threadsWithoutStudents = new ArrayList<>();
        ArrayList<Thread> studentsThreads = new ArrayList<>();

        long tickTime = 0;
        long duration = 0;


//* Start parsing the input file *

        // parsing file
        try {
            String inputFilePath = args[0];

            Object obj = new JSONParser().parse(new FileReader(inputFilePath));

            // typecasting obj to JSONObject
            JSONObject jo = (JSONObject) obj;
            Object curr;
            int index = 1;


//Students
            JSONArray Student = (JSONArray) jo.get("Students");
            Iterator itr = Student.iterator();
            JSONObject model;
            Student newStudent;
            StudentService newStudentService;
            Model newModel;
            String name;
            String department;
            String status;
            String modelName;
            String modelType;
            Data data;
            long modelSize;


            while (itr.hasNext()) {
                curr = itr.next();
                JSONObject currStudent = (JSONObject) curr;
                studentModels = new ArrayList<>();
                name = (String) currStudent.get("name");
                department = (String) currStudent.get("department");
                status = (String) currStudent.get("status");
                newStudent = new Student(name, department, status);
                students.add(newStudent);

                //Models

                JSONArray findModels = (JSONArray) currStudent.get("models");
                Iterator itr5 = findModels.iterator();
                while (itr5.hasNext()) {
                    model = (JSONObject) itr5.next();
                    modelName = (String) model.get("name");
                    modelType = (String) model.get("type");
                    modelSize = (long) model.get("size");
                    data = new Data(modelType, 0, (int) modelSize);
                    newModel = new Model(modelName, data, newStudent);
                    studentModels.add(newModel);
                }
                newStudent.setPreTrainedModels(studentModels);
                newStudentService = new StudentService("name", newStudent);
                newStudent.setStudentService(newStudentService);
                studentsServices.add(newStudentService);
            }


//GPUS

            JSONArray gpu = (JSONArray) jo.get("GPUS");
            Iterator itr1 = gpu.iterator();
            GPUService newGpuService;
            GPU newGpu;
            GPUDataService newGpuDataService;

            while (itr1.hasNext()) {
                curr = itr1.next();
                newGpu = new GPU(curr.toString(), cluster);
                newGpuService = new GPUService("GPU" + index, newGpu);
                newGpuDataService = new GPUDataService("GPU_SERVICE" + index, newGpu);
                index++;

                newGpu.setService(newGpuService);
                newGpu.setDataService(newGpuDataService);

                gpus.add(newGpu);
                gpuServices.add(newGpuService);
                gpuDataServices.add(newGpuDataService);
            }

//CPUS

            JSONArray cpu = (JSONArray) jo.get("CPUS");
            Iterator itr2 = cpu.iterator();
            CPUService newCpuService;
            CPUDataService newCpuDataService;
            CPU newCpu;
            index = 1;


            while (itr2.hasNext()) {
                curr = itr2.next();
                newCpu = new CPU("CPU" + index, (int) (long) curr, cluster);
                newCpuService = new CPUService("CPU_SERVICE" + index, newCpu);
                newCpuDataService = new CPUDataService("CPU_DATA_SERVICE" + index, newCpu);
                newCpu.setTimeService(newCpuService);
                cpus.add(newCpu);
                cpuServices.add(newCpuService);
                cpuDataServices.add(newCpuDataService);
                index++;
            }


//Conferences
            JSONArray conference = (JSONArray) jo.get("Conferences");
            Iterator itr3 = conference.iterator();
            ConfrenceInformation newConference;
            ConferenceService newConferenceService;
            long date;


            while (itr3.hasNext()) {
                curr = itr3.next();
                JSONObject conf = (JSONObject) curr;
                name = (String) conf.get("name");
                date = (long) conf.get("date");
                newConference = new ConfrenceInformation(name, (int) date);
                newConferenceService = new ConferenceService("name", newConference);
                conferences.add(newConference);
                conferencesServices.add(newConferenceService);
            }


//TickTime

            tickTime = (long) jo.get("TickTime");

//Duration

            duration = (long) jo.get("Duration");


        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }


//* End of parsing the input file *


        TimeService timeService = new TimeService((int) duration, (int) tickTime);
        int servicesCount = gpuServices.size() + gpuDataServices.size() + cpuServices.size()
                + cpuDataServices.size() + conferencesServices.size();

        CountDownLatch countDownLatch = new CountDownLatch(servicesCount);

        Thread timeServiceThread = new Thread(timeService);

        //Starting Threads

        for (CPUService cpuService : cpuServices) {
            cpuService.setCountDownLatch(countDownLatch);
            Thread t1 = new Thread(cpuService);
            t1.start();
            threadsWithoutStudents.add(t1);
        }
        for (GPUService gpuService : gpuServices) {
            gpuService.setCountDownLatch(countDownLatch);
            Thread t1 = new Thread(gpuService);
            t1.start();
            threadsWithoutStudents.add(t1);
        }
        for (ConferenceService confService : conferencesServices) {
            confService.setCountDownLatch(countDownLatch);
            Thread t1 = new Thread(confService);
            t1.start();
            threadsWithoutStudents.add(t1);
        }
        for (GPUDataService gpuDataService : gpuDataServices) {
            gpuDataService.setCountDownLatch(countDownLatch);
            Thread t1 = new Thread(gpuDataService);
            t1.start();
            threadsWithoutStudents.add(t1);
        }
        for (CPUDataService cpuDataService : cpuDataServices) {
            cpuDataService.setCountDownLatch(countDownLatch);
            Thread t1 = new Thread(cpuDataService);
            t1.start();
            threadsWithoutStudents.add(t1);
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        timeServiceThread.start();
        for (StudentService studentService : studentsServices) {
            Thread t1 = new Thread(studentService);
            t1.start();
            studentsThreads.add(t1);
        }

        //Joining Threads

        try {
            timeServiceThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            for (Thread thread : threadsWithoutStudents) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            for (Thread thread : studentsThreads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        //Computing Statistics

        for (GPU gpu : gpus) {
            gpuTimeUnitUsed = gpuTimeUnitUsed + gpu.getGpuTimeUnitUsed();
        }

        for (CPU cpu : cpus) {
            cpuTimeUnitUsed = cpuTimeUnitUsed + cpu.getCpuTimeUnitUsed();
            totalDataBatchProcessed = totalDataBatchProcessed + cpu.getTotalDataBatchProcessed();
        }


//* Start generating the output file *

        try {

            PrintWriter output = new PrintWriter("output.json");

            String conferencesStr = "\"conferences\": [" + "\n";
            String publications = "";
            String trained_models = "\"trainedModels\": [" + "\n";
            output.println("{\n" + "\"students\"" + ": [\n");


//student
            int index = students.size();
            for (Student student : students) {
                trained_models = "\"trainedModels\": [" + "\n";
                String student_s = "{\n" + "\"name\":" + " " + "\"" + student.getName() + "\"" + ",\n" + "\"department\":" + " " + "" +
                        "\"" + student.getDepartment() + "\"" + ",\n" + "\"status\":" + " "
                        + "\"" + student.getStatus() + "\"" + ",\n" + "\"publications\":" + " " + student.getPublications() + ",\n"
                        + "\"papersRead\":" + " " + student.getPapersRead() + ",\n";


//Models
                for (Model model : student.getTrainedModels()) {
                    if (model.getStatus().equals(Model.Status.Tested) | model.getStatus().equals(Model.Status.Trained)) {
                        trained_models = trained_models + "{" + "\n" + "\"name\":" + " " + "\"" + model.getName() + "\""
                                + ",\n" + "\"data\":" + " " + "\n" + " {" + "\"type\":" + " " + "\"" + model.getData().getType() + "\"" + ",\n" + "\"size\":"
                                + " " + model.getData().getSize() + "\n}," + "\"status\":" + " " + "\"" + model.getStatus() + "\"" + ",\n" + "\"result\":"
                                + " " + "\"" + model.getResults() + "\"" + "\n" + "}," + "\n";
                    }
                }

                output.println(student_s);
                if (index > 1) {
                    output.println(trained_models + "]" + "}" + ",");
                    index--;
                } else {
                    output.println(trained_models + "]" + "}");
                }
            }
            output.println("],");
            output.println(conferencesStr);


//Conferences

            index = conferences.size();
            for (ConfrenceInformation confrenceInformation : conferences) {
                publications = "";
                String confSTR = "{" + "\n" + "\"name\":" + " " + "\"" + confrenceInformation.getName() + "\""
                        + ",\n" + "\"date\":" + " " + "\"" + confrenceInformation.getDate() + "\"" + ",\n" + "\"publications\":" + " [";

//Publications

                for (Model model : confrenceInformation.getAggregatedModels()) {
                    publications = publications + "{\n" + "\"name\":" + " " + "\"" + model.getName() + "\"" + ",\n"
                            + "\"data\":" + "{\n" + "\"type\":" + " " + "\"" + model.getData().getType() + "\"" + ",\n"
                            + "\"size\":" + " " + model.getData().getSize() + "},\n" + "\"status\":" + " " + "\"" + model.getStatus()
                            + "\"" + ",\n" + "\"results\":" + " " + "\"" + model.getResults() + "\"" + "},\n";
                }

                output.println(confSTR);
                if (index > 1) {
                    output.println(publications + "]" + "}" + ",");
                    index--;
                } else {
                    output.println(publications + "]" + "}");
                }

            }
            output.println("],");


//batches, cpu_time, gpu_time

            String cpu_time = "\"cpuTimeUsed\":" + " " + cpuTimeUnitUsed + ",";
            String gpu_time = "\"gpuTimeUsed\":" + " " + gpuTimeUnitUsed + ",";
            String batchesProcessed = "\"batchesProcessed\":" + " " + totalDataBatchProcessed + "\n}";
            output.println(cpu_time);
            output.println(gpu_time);
            output.println(batchesProcessed);

            output.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


//* Finish generating the output file *

    }
}