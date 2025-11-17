Finance Tracker — Java Budget Management Application

A desktop-based financial management system built in Java that helps users track expenses, manage bank balances, set financial goals, and visualize their progress with interactive charts. The system uses a MySQL database for persistent storage and includes automated monthly updates to simulate real financial cycles.

Features
-User Authentication

Secure user registration and login

Password-protected accounts

Personalized profiles with job title and salary

-Financial Management

Add, edit, and categorize up to 10 expenses

Track monthly salary

Monitor bank balance

Create and manage savings goals

Automatic leftover balance calculation

-Analytics & Charts

Expense breakdown using pie charts

Goal progress visualization

Savings history using bar charts

Real-time financial statistics and summaries

-Automation

Monthly financial cycle simulated every 2 minutes

Goal allocation reset each cycle

Savings recorded automatically

-Technology Stack

Language: Java
GUI: Java Swing
Database: MySQL
Architecture: MVC (Model View Controller)
Charts: Custom Java2D graphics
Database Connector: MySQL Connector/J (JDBC)

Installation & Setup

-Prerequisites

Java JDK 8+
MySQL Server
MySQL Connector/J

1. Create the Database

Copy and run the SQL script located in sqldata.txt inside your MySQL client.
This will create:

users_login
userinfo
expense
goals
bank_account
year

2. Configure Database Credentials

Default connection:

URL: jdbc:mysql://localhost:3306/sign_up  
Username: root  
Password: @PRG123


You may modify credentials directly in the Java files.



-Project Structure

FinanceTracker/
├── LandingPage.java
├── SignUpPage.java
├── SignInPage.java
├── DashboardPage.java
├── ProfilePage.java
├── StatsPage.java
├── ManagePage.java
├── JobSalaryPage.java
├── AppTimer.java
└── sqldata.txt

How It Works
First-Time Setup

Create an account
Enter job and salary details
Enter your current bank balance

Managing Finances

Add expenses with custom categories
Create savings goals
View progress through charts
Update salary or bank balance anytime

Automated Monthly Cycle

The app simulates each month every 2 minutes:
Saves leftover salary after expenses
Updates progress for each goal
Resets monthly expense tracking

Troubleshooting

Database connection error
Ensure MySQL is running
Check username/password
Make sure the sign_up database exists

ClassNotFoundException

Add MySQL Connector/J to your classpath

Tables missing

Re-run sqldata.txt

