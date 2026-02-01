# MergeDJD））））: A Fast Constructive Algorithm with Piece Merging for the Two-Dimensional Irregular Bin Packing Problem

## Introduction

This project introduces a new algorithm, **MergeDJD**, which improves upon the existing DJD heuristic to solve the **Two-Dimensional Irregular Bin Packing Problem**. The problem is based on packing rectangular bins with pieces that are polygons—both convex and non-convex shapes are supported. The shapes contain no curves.

### Applicable Scenarios:

* **Bins**: Rectangular in shape.
* **Pieces**: Convex or non-convex polygonal shapes without any curves.
* **Use Cases**: Suitable for learning, demonstrations, and practical applications.

---

## Getting Started

First, download or clone this project to your local machine.  
Then, navigate to the project root directory and run the following command:

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

After execution, the application will start successfully.
Then, you can access it via your browser at **localhost:8090**.

---
## Demonstration Example of DJD++ Packing
### Step One
The following page is the main entry of the project.

<img width="1893" height="906" alt="image" src="https://github.com/user-attachments/assets/689c04a1-065b-4dc7-920d-8e8096308b69" />

### Step Two
After entering the demonstration page, you can:
- Select a **built-in example**, or
- **Upload your own input files**.

Currently, the supported file formats are:
- `JSON`
- `TXT`

The upload rules and file format specifications can be viewed by clicking the button in the top-right corner of the page.

<img width="1877" height="915" alt="image" src="https://github.com/user-attachments/assets/0955667a-081e-4187-aa45-80d065f54747" />

- The uploaded or selected input data will be displayed in the lower section of the page.

<img width="1876" height="911" alt="image" src="https://github.com/user-attachments/assets/40e9ada3-401b-47da-938b-158c3afc657b" />

### Step Three:
- Click the **Calculate** button to start the packing computation.
- The computation time depends on:
  - The performance of the user's computer
  - The size and complexity of the input data

<img width="1869" height="904" alt="image" src="https://github.com/user-attachments/assets/759b2db3-7c67-434d-80d5-62d3f299ee49" />

---

## System Requirements

* **JDK Version**: jdk-1.8
* **Maven Version**: Apache Maven 3.8.7

---

## Notice

* All input numbers should be **integers**. To improve accuracy, we preprocessed the data by multiplying all coordinates by a scaling factor in experiments.
* If the input file is **.txt**, make sure that each line starts with a **space**.
