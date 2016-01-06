# Question Management Workflow

This document outlines the process for managing application questions using Google Sheets and GitHub, ensuring dynamic content delivery without requiring app recompilation for every question update.

## 1. Google Sheet Structure

The core of this workflow is a single Google Sheet structured to contain all questions, categorized by `paket` (package/version) and `mapel` (subject).

### Required Columns:

| Column Name      | Type    | Description                                                                                                                                                                                                        | Example Value(s)                                   |
| :--------------- | :------ | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :------------------------------------------------- |
| `id`             | Integer | **Optional, but Recommended.** A unique identifier for each question. If not provided, the Room database can auto-generate this.                                                                                     | `1`, `2`, `3`                                      |
| `paket`          | Integer | The package or version number of the question set. This allows for grouping questions.                                                                                                                              | `1`, `2`, `3`                                      |
| `mapel`          | String  | The subject code for the question. These codes *must* match the `MAPEL_CODES` defined in `AppConstants.java` (e.g., "eng", "ind", "mat", "ipa").                                                                  | `"eng"`, `"ind"`                                   |
| `question`       | String  | The full text of the question. HTML tags (`<br>`, `<img>`) are supported and will be rendered by the application. Image paths should be relative to the app's assets (e.g., `questions/img/eng_1_1.png`).         | `"What is the capital of France?`", `"Read the text above to answer questions 1 and 2<br><img src="questions/img/eng_1_1.png"><br>What is the text about?"` |
| `choice0`        | String  | The text for the first answer option.                                                                                                                                                                              | `"London"`                                         |
| `choice1`        | String  | The text for the second answer option.                                                                                                                                                                             | `"Berlin"`                                         |
| `choice2`        | String  | The text for the third answer option.                                                                                                                                                                              | `"Paris"`                                          |
| `choice3`        | String  | The text for the fourth answer option. **Ensure a consistent number of choices for all questions.** You can extend this for more choices (`choice4`, `choice5`, etc.), but the app will need corresponding logic. | `"Rome"`                                           |
| `answer`         | Integer | The 0-based index of the correct answer among the `choice` columns (e.g., `0` for `choice0`, `1` for `choice1`).                                                                                                  | `2`                                                |

### Example Google Sheet Data:

| id | paket | mapel | question                                                                                                                                                            | choice0              | choice1 | choice2 | choice3   | answer |
| :-- | :----- | :----- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :------------------- | :------ | :------ | :-------- | :------ |
| 1  | 1      | eng   | "What is the capital of France?"                                                                                                                                      | "London"             | "Berlin" | "Paris" | "Rome"    | 2      |
| 2  | 1      | ind   | "Siapa presiden pertama Indonesia?"                                                                                                                                   | "Soeharto"           | "Jokowi" | "Sukarno" | "Megawati" | 2      |
| 3  | 2      | eng   | "Which planet is known as the Red Planet?"                                                                                                                            | "Earth"              | "Mars"  | "Jupiter" | "Venus"   | 1      |
| 4  | 1      | eng   | "Read the text above to answer questions 1 and 2<br><img src="questions/img/eng_1_1.png"><br>What is the text about?" | "Opening of a new shop" | "Description of a new home" | "Advertisement on a new restaurant" | "Congratulation to someone on moving to a new house" | 3 |

## 2. Exporting from Google Sheets (Manual Step)

Once your Google Sheet is populated with questions:

1.  Open your Google Sheet.
2.  Go to `File > Share > Publish to web`.
3.  In the "Publish to web" dialog:
    *   Select the entire document or the specific sheet containing your questions.
    *   Choose `Comma-separated values (.csv)` as the format.
    *   Click `Publish`.
4.  Copy the generated URL. This URL is a direct link to the CSV data.

## 3. GitHub Integration

To make your question data accessible via GitHub and version-controlled:

1.  **Save as CSV:** Download the published CSV file from the URL obtained in step 2. You can use `curl` or simply open the URL in a browser and save the file.
    ```bash
    curl -o all_questions.csv "YOUR_GOOGLE_SHEET_CSV_URL_HERE"
    ```
2.  **Add to Git Repository:** Place this `all_questions.csv` file within your GitHub repository. A good practice is to create a dedicated branch (e.g., `quests`) and/or a specific directory (e.g., `data/quests/`) for this.
    ```bash
    # Example: Creating a new branch and adding the file
    git checkout -b quests
    mkdir -p data/quests
    mv all_questions.csv data/quests/
    git add data/quests/all_questions.csv
    git commit -m "Add initial question CSV data"
    git push origin quests
    ```
3.  **Obtain Raw GitHub URL:** Navigate to the `all_questions.csv` file on your GitHub repository in your browser. Look for a "Raw" button. Clicking this button will give you the direct raw content URL. It will typically look like:
    `https://raw.githubusercontent.com/YOUR_USERNAME/YOUR_REPO_NAME/YOUR_BRANCH/path/to/your/file.csv`
    **Example:** `https://raw.githubusercontent.com/koneko096/siapUN/refs/heads/quests/all.csv`

## 4. App-Side Processing

1.  **Update `AppConstants.java`:**
    Modify the `GSHEET_CSV_URL` in `app/src/main/java/io/github/koneko096/siapun/AppConstants.java` to use the raw GitHub URL obtained in the previous step.
    ```java
    // app/src/main/java/io/github/koneko096/siapun/AppConstants.java
    public class AppConstants {
        // ... other constants
        public static final String GSHEET_CSV_URL = "https://raw.githubusercontent.com/koneko096/siapUN/refs/heads/quests/all.csv";
    }
    ```
2.  **`SyncWorker` Logic:**
    The `SyncWorker` (defined in `io.github.koneko096.siapun.sync.SyncWorker.java`) is responsible for:
    *   Fetching the CSV content from the `GSHEET_CSV_URL` using `GSheetService`.
    *   Parsing the CSV rows.
    *   Transforming each row into a `Soal` object, mapping columns like `paket`, `mapel`, `question`, creating a list from `choice0`, `choice1`, etc., for `choices`, and setting `answer`.
    *   Storing these `Soal` objects into the local Room database via `SoalDao`.

    **Note:** The current `GSheetService` downloads a `ResponseBody`. You will need to implement the CSV parsing logic within the `SyncWorker` or a helper class to convert the CSV content into `List<Soal>` objects.

## 5. Considerations

*   **Manual Sync:** If you primarily edit questions in Google Sheets, remember that updating the GitHub CSV is a *manual step*. Any changes in the Google Sheet will not automatically reflect on GitHub.
*   **Public Data:** The CSV file hosted on GitHub's raw content service is publicly accessible. Do not include any sensitive or private information in this file.
*   **Version Control Benefits:** By hosting on GitHub, you gain full version control over your question data. You can easily track changes, revert to previous versions, and manage different sets of questions using Git branches.
*   **Image Assets:** Ensure that any image assets referenced in the `question` field (e.g., `img/eng_1_1.png`) are also included in your app's `assets` folder or served from a publicly accessible URL, as the app will load them based on the path provided. Images referenced in the CSV will need to be present in `app/src/main/assets/questions/img/` for the app to display them correctly.
