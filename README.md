# DJD++: Improving the DJD Heuristic for the Two-Dimensional Irregular Bin Packing Problem

## Introduction

This project introduces a new algorithm, **DJD++**, which improves upon the existing DJD heuristic to solve the **Two-Dimensional Irregular Bin Packing Problem**. The problem is based on packing rectangular bins with pieces that are polygons—both convex and non-convex shapes are supported. The shapes contain no curves.

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

![DJD++ Algorithm](https://github.com/user-attachments/assets/1643c2b6-b08a-434e-bade-f93a26152bd8)

### Step Two
After entering the demonstration page, you can:
- Select a **built-in example**, or
- **Upload your own input files**.

Currently, the supported file formats are:
- `JSON`
- `TXT`

The upload rules and file format specifications can be viewed by clicking the button in the top-right corner of the page.
<img width="1884" height="908" alt="image" src="https://github.com/user-attachments/assets/70ac7803-017b-4658-9a80-9b014dcbe8bf" />
- The uploaded or selected input data will be displayed in the lower section of the page.

<img width="1880" height="905" alt="image" src="https://github.com/user-attachments/assets/ce613ef7-61d5-46ff-bd81-234d4c7b5600" />

### Step Three:
- Click the **Calculate** button to start the packing computation.
- The computation time depends on:
  - The performance of the user's computer
  - The size and complexity of the input data

<img width="1866" height="896" alt="image" src="https://github.com/user-attachments/assets/683a2fae-d7ec-49b6-9168-016983755a90" />

---

## System Requirements

* **JDK Version**: jdk-1.8
* **Maven Version**: Apache Maven 3.8.7

---

## Notice

* If the input file is **.txt**, make sure that each line starts with a **space**.
* The web visualization uses the top-left corner of each bin as the origin, while our code uses the bottom-left corner. To view the packing result in the **correct orientation**, simply flip the images upside down.
