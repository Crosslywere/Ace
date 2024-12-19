# Ace Exam App

The Ace Exam Platform Application.

If this was opened when attempting to run the build script, **NOTE** the script is still running in the background waiting for your input to proceed.

Things can be halted anytime by pressing `CTRL` + `C` in the host's terminal while the script / application is running. 

## Dependencies

1. [PostgreSQL](https://www.postgresql.org/download/)

2. [Java](https://www.oracle.com/ng/java/technologies/downloads/)

## Configuration

To configure the application navigate to `src/main/resources/` and open `application.properties` in a text editor. Inside of this file the following properties should be used to configure the application.

**`spring.datasource.username`** - This is the username with administrator privileges on the PostgreSQL database.

**`spring.datasource.password`** - This is the password of the username in the `spring.datasource.username`

**`spring.mail.username`** - This is the email address to be used to send notification emails to Candidates when creating exams.

**`spring.mail.password`** - This is the app password linked to the account can be gotten from [here](https://myaccount.google.com/apppasswords)

### Configure PostgreSQL

Before attempting to configure the application ensure that you have a valid database setup to connect too.

- Begin by opening the pgAdmin application that comes with the installation of [Postgres](https://www.postgresql.org/download/)
- Next, on the left there would be a section for servers, right-click on the Servers and select Register -> Server.
- You'll be greeted by the General tab where you would give the server a name, any name is fine.
<center>
   <img src="./README/PG-General.png">
</center>

- Finally, go to the Connection tab and give the database the Host name of `localhost`, set the Password and possibly change the username.
<center>
   <img src="./README/PG-Connection.png">
</center>

- Click `Save` to create the database.

## Installation

Before installing the application ensure that the [Dependencies](#dependencies) are installed and you have [Configured](#configuration) the application.

To install this application simply click on the `build.bat` / `build` file.

Subsequent runs can be started by clicking on the `run.bat` / `run` file.

## Using The Application

Once completing the [installation](#installation) process, open a browser and go to this url [localhost:8081](http://localhost:8081) which would bring you to the `/scheduled` page.

The possible routes that can be visited are:

* [`/scheduled`](#scheduled-exams) - view and manage all scheduled exams

* [`/recorded`](#recorded-exams) - view and manage all recorded exams

* [`/ongoing`](#ongoing-exams) - view and manage all ongoing exams

* [`/create`](#creating-an-exam) - create new exams

* [`/exam` or `/exam/select`](#taking-an-exam) - take any ongoing exams

### Scheduled Exams

A **scheduled exam** is any exam whose time is not yet now ie. the starting time is not passed and/or has no candidates attached yet.

The scheduled exams page allows any scheduled exam to be modified or deleted.

#### Modifying Scheduled Exams

Modifying a scheduled exam allows you to update:

1. The title of the exam. Signifies the exam being held eg. JAMB, WAEC, etc.

2. The scheduled date (cannot be before the current date). When the exam is scheduled for.

3. The start time (should not be after the end time). When the exam should open to candidates.

4. The end time (should not be before the start time). When the exam should close to candidates.

5. The duration (in minutes). How long a candidate is allowed to spend taking the exam.

6. Weather to show the results to the candidate after they have concluded the exam or not.

7. Modify the papers in the exam. Please see [Exam Document Formats](#exam-document-formats) for how to organize an exam paper for upload.

    * Change the paper's name.
    * Change the questions per candidate.
    * Change whether the paper is mandatory or not.
    * Upload images to questions as needed.

8. Modify the list of candidates for the exam by uploading `.csv` files. Please see [Exam Document Formats](#exam-document-formats) for how to organize a candidate list for upload.

### Recorded Exams

Recorded exams are exams that have been concluded and can no longer be taken by any candidate. They can only either be exported or deleted from the system **(which can not be recovered)**

To export exams simply click on "Export" on the row of the exam you wish to export

#### Exporting Recorded Exams

Exporting recorded exams requires the export to be configured. Once configured the exam can be downloaded as a .csv file

### Ongoing Exams

An ongoing exam can be stopped forcefully by clicking on "Stop" or [modified](#modifying-ongoing-exams) by clicking on "Modify".

#### Modifying Ongoing Exams

Exams do not have much that can be modified about them when in an ongoing state. The only thing that can be modified about them is the time when they close to candidates and their title.
You can however [manage candidates](#managing-candidates).

#### Managing Candidates

Managing candidates can be done through the "MANAGEMENT" column of the table. Logout _OR_ Reset

- **Logout** This simply logs the candidate out on the server once the candidate login or performs any action they are set as logged in again.
- **Reset** This removes all the candidate's questions, and resets all their details. If the candidate is logged in this will force them back to the exam select page.

### Creating an Exam

To create an exam there are 2 files that would be needed by the software:

1. The Exam Document - This can either be a `.docx`(Microsoft Word Document) or a `.txt`(Plain Text) file.

2. The Candidate List - This can only be a `.csv`(Comma Seperated Value) file. Which would contain the following candidates details:

Please see [Exam Document Formats](#exam-document-formats) for how to create these documents for the software.

It is best to go step by step when creating an exam. From setting the exam's general details, to uploading the exams document and setting up the papers, to finally uploading the candidates document.
Because once there could be some error at a previous set causing the proceeding step to have errors.

#### Exam Document Formats

Exam documents are made up of 4 things

1. The paper indicator. This indicates where a paper starts from. Eg "Paper:", "Mathematics:", "English Language:" etc. The format is as follows: The name of the paper followed by a colon(:)
2. The question. Eg "1. What is the cap...", "300. There are 2 types of..." etc. The format is as follows: The number of the question, a period(.), a space, and finally the text representing the question(not broken by a new line(Enter)).
3. The options. The options can be 2 to 5 in number. Eg "a. Nigeria", "b. Canada", "c. Europe", "d. India", "e. China" etc. The format is as follows: The index of the option ie. a-e as a small letters, a period(.), a space, and the text for the option.
4. The answer. The answer is optional. Eg. "Ans: a", "Ans: b" etc. The format is as follows: The text "Ans:" the colon is necessary, a space, and finally the text for the option.

For exams please open [this](Samples/Sample exam.txt) file for an example

Candidate documents are made up of 11 things

1. The username of the candidate. This is any column in the .csv file marked with "#UN" followed by a space. This is required to identify the candidates for logging in.
2. The password of the candidate. This is any column in the .csv file marked with "#PW" followed by a space.
3. The papers of the candidate. This is any column containing the word "paper". This is required to specify the candidate's papers. Is required but can be left blank if all papers are mandatory.
4. The candidate's registration number. This is any column containing the word "reg", "serial", or "s/n".
5. The candidate's email. This is any column that contains the word "mail". Is not necessary but is if the exam is to send a notification to candidates on the details.
6. The candidate's phone number. This is any column that contains the word "phone" or "tel".
7. The candidate's address. This is any column that contains the word "address".
8. The candidate's state. This is any column that contains the word "state".
9. The candidate's firstname. This is any column that contains the words "name" and "first"
10. The candidate's lastname. This is any column that contains the words "name" and "last"
11. The candidate's other names. This is any column that contains the words "name" and "other"

For candidate lists please open [this](Samples/Sample candidate.csv) file for an example

### Taking an Exam

When taking an exam the candidate's will be greeted with the exam selection page. Once they have selected the exam, they will then proceed to the login page.
After the candidate provides the required username and/or password, they will then be greeted with the instructions page. After reading the instructions they may proceed to the exam proper.

- Once the candidate logs in the candidate's questions are set.
- The candidates have timer's on the top right side of the question. Once this time runs out the candidate if forced to submit their exam.
- The candidates have the ability to select papers by clicking on the paper name on the top of the page.
- The candidates can navigate through the question by using the left and right arrows.
- The candidates can select options by pressing a,b,c,d, or e.
