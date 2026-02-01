package com.example.demo.controller;

import com.example.demo.share.Piece;
import com.example.demo.share.RunHeuristics;
import com.example.demo.share.Sheet;
import com.example.demo.utils.Container;
import com.example.demo.utils.InputData;
import com.example.demo.utils.Polygon;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("/polygon")
@RestController
@CrossOrigin
public class PolygonController {
    @PostMapping("/input")
    public List<Container> inputData(@RequestBody InputData inputData) throws Exception {
        inputData.setNum(inputData.getPieces().size());
        int numHeuristics = 1;
        List<Sheet> sheets = RunHeuristics.run_input_data(numHeuristics, inputData);
        List<Container> containers = new ArrayList<>();
        for (Sheet sheet : sheets) {
            Container container = new Container();
            container.setWidth(sheet.getXmax());
            container.setHeight(sheet.getYmax());
            List<Polygon> polygons = new ArrayList<>();
            for (Piece piece : sheet.getPzasInside()) {
                Polygon polygon = new Polygon();
                List<Integer> x = new ArrayList<>();
                for (int coordX : piece.coordX) {
                    x.add(coordX);
                }
                polygon.setX(x);
                List<Integer> y = new ArrayList<>();
                for (int coordY : piece.coordY) {
                    y.add(coordY);
                }
                polygon.setY(y);
                polygons.add(polygon);
            }
            container.setPolygons(polygons);
            containers.add(container);
        }
        return containers;
    }
}