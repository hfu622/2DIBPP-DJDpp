package com.example.demo.share;

import com.example.demo.utils.InputData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class RunHeuristics {

    public static void main(String[] args) throws Exception {

        String instances = "JP1G";

        File dirSingle = new File("D:/RESEARCH/DJD/NestingData/");

        File dirAllInstance = new File("D:/RESEARCH/DJD/NestingData/");

        File dirSolution = new File("D:/RESEARCH/DJD/results1/" + instances + "/");

        File filePath = new File("D:/develop/BPP-server/demo/src/main/resources/results/");

        int numHeuristics = 1;

        boolean repetition = true;
        boolean graphVisual = true;

        if (!filePath.exists())
            filePath.mkdir();

        if (!dirSolution.exists())
            dirSolution.mkdir();

        File archieveProblems = new File(dirSingle, instances + ".txt");

        System.out.println("Solving instances: " + instances);
        RunHeuristics.run(dirAllInstance, dirSolution, filePath, archieveProblems,
                instances, numHeuristics, repetition, graphVisual);
        System.out.println("Finish");
        System.out.println();
    }

    public RunHeuristics() {
    }

    public static List<Sheet> run_input_data(int numHeuristicas, InputData inputData) throws Exception {
        List<Sheet> sheets = new ArrayList<Sheet>();
        RWfiles rw = new RWfiles();
        List<String> problems = new ArrayList<String>();


        problems.add("problem");
        int numproblems = problems.size();
        double[][] aptitudes = new double[numproblems][numHeuristicas];
        int[][] numObjects = new int[numproblems][numHeuristicas];
        int[][] executionTime = new int[numproblems][numHeuristicas];
        double[][] utilizations = new double[numproblems][numHeuristicas];
        double[][] Kvalues = new double[numproblems][numHeuristicas];

        int indice = 0;
        Iterator<String> iter = problems.iterator();
        while (iter.hasNext()) {
            Instance p = new Instance(numproblems * numHeuristicas);
            String problem = iter.next();
            for (int i = 0; i < numHeuristicas; i++) {
                System.out.println("Solving " + problem + " with heuristic " + i);

                p.obtainProblemInfo(inputData);

                long start = System.currentTimeMillis();

                Results DJDoriginalresult = p.execute(i, indice, false);

                sheets = DJDoriginalresult.getListaObjetos();

                executionTime[indice][i] = DJDoriginalresult.getUsedtime();
                numObjects[indice][i] = DJDoriginalresult.getBinnum();
                aptitudes[indice][i] = DJDoriginalresult.getAptitude();
                Kvalues[indice][i] = DJDoriginalresult.getKvalue();
                utilizations[indice][i] = DJDoriginalresult.getUtilization();

                List<Sheet> finalBin = DJDoriginalresult.getListaObjetos();

            }

            indice++;
        }

        return sheets;
    }

    public static void run(File dirAllInstance, File dirSolution, File filePath, File archieveProblems,
                           String instances, int numHeuristicas, boolean repeticion, boolean graphVisual) throws Exception {

        RWfiles rw = new RWfiles();
        List<String> problems = new ArrayList<String>();
        File instancesSolution0 = new File(dirSolution, "solution_" + instances);
        File instancesSolution = new File(dirSolution, "solution_" + instances);

        if (!instancesSolution.exists() || instancesSolution.length() == 0 || repeticion) {
            try {
                problems = rw.loadProblems(archieveProblems);

            } catch (Exception e) {
                System.err.println("Error in reading listOfInstances: " + archieveProblems);
                System.exit(0);
            }

            int numproblems = problems.size();
            double[][] aptitudes = new double[numproblems][numHeuristicas];
            int[][] numObjects = new int[numproblems][numHeuristicas];
            int[][] executionTime = new int[numproblems][numHeuristicas];
            double[][] utilizations = new double[numproblems][numHeuristicas];
            double[][] Kvalues = new double[numproblems][numHeuristicas];

            int indice = 0;
            Iterator<String> iter = problems.iterator();
            while (iter.hasNext()) {
                Instance p = new Instance(numproblems * numHeuristicas);
                String problem = iter.next();
                File instancesOut = new File(filePath, "solution_" + problem);
                if (!instancesOut.exists() || instancesOut.length() == 0 || repeticion == true) {
                    PrintWriter printerWriter = new PrintWriter(instancesOut);
                    for (int i = 0; i < numHeuristicas; i++) {
                        System.out.println("Solving " + problem + " with heuristic " + i);

                        if (problem.endsWith("txt")) {
                            p.obtainProblem(new File(dirAllInstance, problem));
                        } else {
                            p.obtainProblemExcel(new File(dirAllInstance, problem));
                        }

                        long start = System.currentTimeMillis();

                        Results DJDoriginalresult = p.ejecutaAccion(i, indice, graphVisual);

                        executionTime[indice][i] = DJDoriginalresult.getUsedtime();
                        numObjects[indice][i] = DJDoriginalresult.getBinnum();
                        aptitudes[indice][i] = DJDoriginalresult.getAptitude();
                        Kvalues[indice][i] = DJDoriginalresult.getKvalue();
                        utilizations[indice][i] = DJDoriginalresult.getUtilization();

                        List<Sheet> finalBin = DJDoriginalresult.getListaObjetos();

                        printerWriter.println(i + "," + executionTime[indice][i] + "," + numObjects[indice][i] + "," +
                                aptitudes[indice][i] + "," + Kvalues[indice][i] + "," + utilizations[indice][i]);
                    }
                    printerWriter.close();

                } else {
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(instancesOut));
                        String line = null;
                        String[] lineBreak;
                        for (int i = 0; i < numHeuristicas; i++) {
                            line = reader.readLine();
                            lineBreak = line.split(",");
                            aptitudes[indice][i] = Double.valueOf(lineBreak[1]);
                            numObjects[indice][i] = Integer.valueOf(lineBreak[2]);
                        }
                        reader.close();
                    } catch (Exception e) {
                        System.err.println("error: " + instancesOut);
                    }
                }
                indice++;
            }

        }
    }

    public static List<List<Sheet>> run1(File dirAllInstance, File dirSolution, File filePath, File archieveProblems,
                                         String instances, int numHeuristicas, boolean repeticion, boolean graphVisual) throws Exception {

        List<List<Sheet>> sheets = new ArrayList<>();
        RWfiles rw = new RWfiles();
        List<String> problems = new ArrayList<String>();
        File instancesSolution0 = new File(dirSolution, "solution_" + instances);
        File instancesSolution = new File(dirSolution, "solution_" + instances);

        if (!instancesSolution.exists() || instancesSolution.length() == 0 || repeticion) {
            try {
                problems = rw.loadProblems(archieveProblems);

            } catch (Exception e) {
                System.err.println("Error in reading listOfInstances: " + archieveProblems);
                System.exit(0);
            }

            int numproblems = problems.size();
            double[][] aptitudes = new double[numproblems][numHeuristicas];
            int[][] numObjects = new int[numproblems][numHeuristicas];
            int[][] executionTime = new int[numproblems][numHeuristicas];
            double[][] utilizations = new double[numproblems][numHeuristicas];
            double[][] Kvalues = new double[numproblems][numHeuristicas];

            int indice = 0;
            Iterator<String> iter = problems.iterator();
            while (iter.hasNext()) {
                Instance p = new Instance(numproblems * numHeuristicas);
                String problem = iter.next();
                File instancesOut = new File(filePath, "solution_" + problem);
                if (!instancesOut.exists() || instancesOut.length() == 0 || repeticion == true) {
                    PrintWriter printerWriter = new PrintWriter(instancesOut);
                    for (int i = 0; i < numHeuristicas; i++) {
                        System.out.println("Solving " + problem + " with heuristic " + i);

                        if (problem.endsWith("txt")) {
                            p.obtainProblem(new File(dirAllInstance, problem));
                        } else {
                            p.obtainProblemExcel(new File(dirAllInstance, problem));
                        }

                        long start = System.currentTimeMillis();

                        Results DJDoriginalresult = p.ejecutaAccion(i, indice, graphVisual);

                        System.out.println(DJDoriginalresult.getListaObjetos());
                        sheets.add(DJDoriginalresult.getListaObjetos());

                        executionTime[indice][i] = DJDoriginalresult.getUsedtime();
                        numObjects[indice][i] = DJDoriginalresult.getBinnum();
                        aptitudes[indice][i] = DJDoriginalresult.getAptitude();
                        Kvalues[indice][i] = DJDoriginalresult.getKvalue();
                        utilizations[indice][i] = DJDoriginalresult.getUtilization();

                        List<Sheet> finalBin = DJDoriginalresult.getListaObjetos();

                        printerWriter.println(i + "," + executionTime[indice][i] + "," + numObjects[indice][i] + "," +
                                aptitudes[indice][i] + "," + Kvalues[indice][i] + "," + utilizations[indice][i]);
                    }
                    printerWriter.close();

                } else {
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(instancesOut));
                        String line = null;
                        String[] lineBreak;
                        for (int i = 0; i < numHeuristicas; i++) {
                            line = reader.readLine();
                            lineBreak = line.split(",");
                            aptitudes[indice][i] = Double.valueOf(lineBreak[1]);
                            numObjects[indice][i] = Integer.valueOf(lineBreak[2]);
                        }
                        reader.close();
                    } catch (Exception e) {
                        System.err.println("error : " + instancesOut);
                    }
                }
                indice++;
            }

        }
        return sheets;
    }
}
