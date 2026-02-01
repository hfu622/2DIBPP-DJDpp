package com.example.demo.controller;

import com.example.demo.demos.web.User;
import com.example.demo.share.RunHeuristics;
import com.example.demo.share.Sheet;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("/test")
@RestController
@CrossOrigin
public class TestController {
    @GetMapping("/list")
    public List<User> getAllUsers(){
        List<User> userList = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            User user = new User();
            user.setName("user"+i);
            user.setAge(18+i);
            userList.add(user);
        }
        return userList;
    }

    @GetMapping("/results")
    public List<List<Sheet>> getResults() throws Exception {
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
        List<List<Sheet>> lists = RunHeuristics.run1(dirAllInstance, dirSolution, filePath, archieveProblems,
                instances, numHeuristics, repetition, graphVisual);
        System.out.println("Finish");
        System.out.println();
        return lists;
    }
}