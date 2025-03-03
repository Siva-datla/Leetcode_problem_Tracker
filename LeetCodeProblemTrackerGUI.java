import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeetCodeProblemTrackerGUI {
    private static final String DATA_FILE = "problems.json";
    private static final Gson gson = new Gson();

    public static class Problem {
        long id;
        String title;
        String difficulty;
        List<String> tags;
        String date;
        String notes;

        public Problem(long id, String title, String difficulty, List<String> tags, String date, String notes) {
            this.id = id;
            this.title = title;
            this.difficulty = difficulty;
            this.tags = tags;
            this.date = date;
            this.notes = notes;
        }
    }

    public static List<Problem> loadProblems() {
        List<Problem> problems = new ArrayList<>();
        try {
            File file = new File(DATA_FILE);
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                Type listType = new TypeToken<List<Problem>>() {}.getType();
                problems = gson.fromJson(reader, listType);
                reader.close();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error loading problems: " + e.getMessage());
        }
        return problems;
    }

    public static void saveProblems(List<Problem> problems) {
        try (Writer writer = new FileWriter(DATA_FILE)) {
            gson.toJson(problems, writer);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving problems: " + e.getMessage());
        }
    }

    public static int computeLongestStreak(List<Problem> problems) {
        if (problems.isEmpty()) {
            return 0;
        }
        List<LocalDate> dates = new ArrayList<>();
        for (Problem p : problems) {
            try {
                dates.add(LocalDate.parse(p.date));
            } catch (Exception e) {
                // Skip 
            }
        }
        if (dates.isEmpty()) {
            return 0;
        }
        Collections.sort(dates);
        int longestStreak = 1;
        int currentStreak = 1;
        for (int i = 1; i < dates.size(); i++) {
            if (dates.get(i).equals(dates.get(i - 1).plusDays(1))) {
                currentStreak++;
            } else {
                currentStreak = 1;
            }
            if (currentStreak > longestStreak) {
                longestStreak = currentStreak;
            }
        }
        return longestStreak;
    }

    public static void addProblemGUI() {
        JTextField titleField = new JTextField(20);
        JTextField difficultyField = new JTextField(10);
        JTextField tagsField = new JTextField(20);
        JTextField dateField = new JTextField(10);
        JTextField notesField = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Difficulty (Easy/Medium/Hard):"));
        panel.add(difficultyField);
        panel.add(new JLabel("Tags (comma separated):"));
        panel.add(tagsField);
        panel.add(new JLabel("Date (YYYY-MM-DD):"));
        panel.add(dateField);
        panel.add(new JLabel("Notes:"));
        panel.add(notesField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Add Problem",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String difficulty = difficultyField.getText().trim();
            String tagsInput = tagsField.getText().trim();
            String dateInput = dateField.getText().trim();
            String notes = notesField.getText().trim();

            try {
                LocalDate.parse(dateInput);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(null, "Invalid date format. Please use YYYY-MM-DD.");
                return;
            }

            List<String> tags = new ArrayList<>();
            if (!tagsInput.isEmpty()) {
                String[] arr = tagsInput.split(",");
                for (String tag : arr) {
                    tags.add(tag.trim());
                }
            }

            List<Problem> problems = loadProblems();
            Problem newProblem = new Problem(System.currentTimeMillis(), title, difficulty, tags, dateInput, notes);
            problems.add(newProblem);
            saveProblems(problems);
            JOptionPane.showMessageDialog(null, "Problem added successfully!");
        }
    }

    public static void listProblemsGUI() {
        List<Problem> problems = loadProblems();
        if (problems.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No problems added yet.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Problem p : problems) {
            sb.append("ID: ").append(p.id).append("\n");
            sb.append("Title: ").append(p.title).append("\n");
            sb.append("Difficulty: ").append(p.difficulty).append("\n");
            sb.append("Date: ").append(p.date).append("\n");
            sb.append("Tags: ").append(String.join(", ", p.tags)).append("\n");
            sb.append("Notes: ").append(p.notes).append("\n");
            sb.append("---------------------------------\n");
        }
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        JOptionPane.showMessageDialog(null, scrollPane, "List of Problems", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showDashboardGUI() {
        List<Problem> problems = loadProblems();
        int total = problems.size();
        int longestStreak = computeLongestStreak(problems);
        String message = "Total Problems Solved: " + total +
                "\nLongest Consecutive Day Streak: " + longestStreak + " day(s)";
        JOptionPane.showMessageDialog(null, message, "Dashboard", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void createAndShowGUI() {
        JFrame frame = new JFrame("LeetCode Problem Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new FlowLayout());

        JButton addButton = new JButton("Add Problem");
        JButton listButton = new JButton("List Problems");
        JButton dashboardButton = new JButton("Dashboard");
        JButton exitButton = new JButton("Exit");

        addButton.addActionListener(e -> addProblemGUI());
        listButton.addActionListener(e -> listProblemsGUI());
        dashboardButton.addActionListener(e -> showDashboardGUI());
        exitButton.addActionListener(e -> System.exit(0));

        frame.add(addButton);
        frame.add(listButton);
        frame.add(dashboardButton);
        frame.add(exitButton);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }
}
