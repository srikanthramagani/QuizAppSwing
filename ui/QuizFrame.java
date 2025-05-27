package ui;

import model.Question;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.Timer;

import java.util.*;
import java.util.List;

public class QuizFrame extends JFrame implements ActionListener {
    private Question[] questions;
    private int current = 0;
    private int score = 0;
    private final int QUESTIONS_PER_ROUND = 10;

    private JTextArea questionArea;
    private JRadioButton[] options;
    private ButtonGroup optionGroup;
    private JButton nextButton, submitButton, toggleThemeButton;
    private JLabel timerLabel;
    private JProgressBar progressBar;
    private Timer timer;
    private int timeLeft = 15;

    private boolean isDarkMode = false;
    private Color lightBg = new Color(225, 245, 254); // Light blue-white
    private Color lightFg = new Color(13, 71, 161); // Deep blue
    private Color darkBg = new Color(25, 25, 25); // Darker gray
    private Color darkFg = new Color(245, 245, 245); // Light gray
    private Color accentColor = new Color(40, 167, 69); // Vibrant green
    private float fadeOpacity = 0.0f; // For question fade-in animation
    private Timer fadeTimer;
    private float buttonScale = 1.0f; // For button click animation
    private Timer buttonScaleTimer;

    public QuizFrame() {
        setTitle("General Knowledge Quiz Master");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        questions = loadQuestions();

        // North Panel - Question + Timer + Theme Toggle
        JPanel topPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, isDarkMode ? darkBg : lightBg, 
                                              getWidth(), getHeight(), isDarkMode ? new Color(40, 40, 40) : new Color(179, 229, 252)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(new Color(0, 0, 0, 20));
                for (int i = -getHeight(); i < getWidth(); i += 20) {
                    g2d.drawLine(i, 0, i + getHeight(), getHeight());
                }
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.drawRect(5, 5, getWidth() - 10, getHeight() - 10);
            }
        };
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        questionArea = new JTextArea(4, 50);
        questionArea.setFont(new Font("SansSerif", Font.BOLD, 22));
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setEditable(false);
        questionArea.setOpaque(false);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.gridwidth = 2;
        topPanel.add(questionArea, gbc);

        timerLabel = new JLabel("Time Left: 15s");
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        timerLabel.setForeground(isDarkMode ? new Color(255, 182, 193) : new Color(200, 0, 0));
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.5; gbc.gridwidth = 1;
        topPanel.add(timerLabel, gbc);

        toggleThemeButton = new JButton("Light Mode");
        toggleThemeButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        toggleThemeButton.addActionListener(e -> toggleTheme());
        styleButton(toggleThemeButton);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.5;
        topPanel.add(toggleThemeButton, gbc);

        add(topPanel, BorderLayout.NORTH);

        // Center Panel - Options
        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 10, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeOpacity));
                g2d.setPaint(new GradientPaint(0, 0, isDarkMode ? darkBg : lightBg, 
                                              getWidth(), getHeight(), isDarkMode ? new Color(40, 40, 40) : new Color(179, 229, 252)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(new Color(0, 0, 0, 20));
                for (int i = -getHeight(); i < getWidth(); i += 20) {
                    g2d.drawLine(i, 0, i + getHeight(), getHeight());
                }
            }
        };
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));

        options = new JRadioButton[4];
        optionGroup = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton();
            options[i].setFont(new Font("SansSerif", Font.PLAIN, 18));
            options[i].setOpaque(false);
            optionGroup.add(options[i]);
            centerPanel.add(options[i]);
        }
        add(centerPanel, BorderLayout.CENTER);

        // Bottom Panel - Progress + Buttons
        JPanel bottomPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, isDarkMode ? darkBg : lightBg, 
                                              getWidth(), getHeight(), isDarkMode ? new Color(40, 40, 40) : new Color(179, 229, 252)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(new Color(0, 0, 0, 20));
                for (int i = -getHeight(); i < getWidth(); i += 20) {
                    g2d.drawLine(i, 0, i + getHeight(), getHeight());
                }
            }
        };
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        progressBar = new JProgressBar(0, QUESTIONS_PER_ROUND);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString("Progress: 0/" + QUESTIONS_PER_ROUND);
        progressBar.setForeground(accentColor);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.gridwidth = 2;
        bottomPanel.add(progressBar, gbc);

        nextButton = new JButton("Next");
        nextButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        nextButton.setPreferredSize(new Dimension(80, 30));
        nextButton.addActionListener(this);
        styleButton(nextButton);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.5; gbc.gridwidth = 1;
        bottomPanel.add(nextButton, gbc);

        submitButton = new JButton("Submit");
        submitButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        submitButton.setPreferredSize(new Dimension(80, 30));
        submitButton.setEnabled(false);
        submitButton.addActionListener(this);
        styleButton(submitButton);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.5;
        bottomPanel.add(submitButton, gbc);

        add(bottomPanel, BorderLayout.SOUTH);

        loadQuestion(current);
        startTimer();
        applyTheme();

        setVisible(true);
    }

    private void styleButton(JButton button) {
        button.setBackground(isDarkMode ? new Color(50, 50, 50) : accentColor);
        button.setForeground(isDarkMode ? darkFg : lightFg);
        button.setBorder(BorderFactory.createLineBorder(accentColor, 2, true));
        button.setFocusPainted(false);
        addHoverEffect(button);
        addClickAnimation(button);
    }

    private void addHoverEffect(JButton button) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(isDarkMode ? new Color(70, 70, 70) : new Color(28, 140, 58));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(isDarkMode ? new Color(50, 50, 50) : accentColor);
            }
        });
    }

    private void addClickAnimation(JButton button) {
        button.addActionListener(e -> {
            buttonScale = 0.9f;
            if (buttonScaleTimer != null) buttonScaleTimer.stop();
            buttonScaleTimer = new Timer(50, evt -> {
                buttonScale += 0.05f;
                button.setFont(new Font("SansSerif", Font.BOLD, (int)(12 * buttonScale)));
                button.repaint();
                if (buttonScale >= 1.0f) {
                    ((Timer)evt.getSource()).stop();
                    button.setFont(new Font("SansSerif", Font.BOLD, 12));
                }
            });
            buttonScaleTimer.start();
        });
    }

    private Question[] loadQuestions() {
        Question[] allQuestions = new Question[50];

        allQuestions[0] = new Question(
            "What is the capital city of France?",
            new String[]{"Paris", "London", "Berlin", "Rome"},
            1
        );
        allQuestions[1] = new Question(
            "Which planet is known as the Red Planet?",
            new String[]{"Jupiter", "Mars", "Venus", "Mercury"},
            2
        );
        allQuestions[2] = new Question(
            "Who painted the Mona Lisa?",
            new String[]{"Vincent van Gogh", "Pablo Picasso", "Leonardo da Vinci", "Claude Monet"},
            3
        );
        allQuestions[3] = new Question(
            "What is the largest ocean on Earth?",
            new String[]{"Atlantic Ocean", "Indian Ocean", "Arctic Ocean", "Pacific Ocean"},
            4
        );
        allQuestions[4] = new Question(
            "Which gas do plants absorb during photosynthesis?",
            new String[]{"Oxygen", "Carbon Dioxide", "Nitrogen", "Hydrogen"},
            2
        );
        allQuestions[5] = new Question(
            "Who wrote the play 'Romeo and Juliet'?",
            new String[]{"William Shakespeare", "Charles Dickens", "Jane Austen", "Mark Twain"},
            1
        );
        allQuestions[6] = new Question(
            "What is the chemical symbol for gold?",
            new String[]{"Ag", "Au", "Fe", "Cu"},
            2
        );
        allQuestions[7] = new Question(
            "Which country hosted the 2016 Summer Olympics?",
            new String[]{"China", "Brazil", "Japan", "United Kingdom"},
            2
        );
        allQuestions[8] = new Question(
            "What is the smallest country in the world by land area?",
            new String[]{"Monaco", "Vatican City", "Maldives", "Liechtenstein"},
            2
        );
        allQuestions[9] = new Question(
            "Which animal is known as the 'King of the Jungle'?",
            new String[]{"Elephant", "Tiger", "Lion", "Gorilla"},
            3
        );
        allQuestions[10] = new Question(
            "What is the longest river in the world?",
            new String[]{"Amazon River", "Nile River", "Yangtze River", "Mississippi River"},
            2
        );
        allQuestions[11] = new Question(
            "Who discovered the theory of relativity?",
            new String[]{"Isaac Newton", "Albert Einstein", "Galileo Galilei", "Nikola Tesla"},
            2
        );
        allQuestions[12] = new Question(
            "What is the currency of Japan?",
            new String[]{"Yuan", "Won", "Yen", "Ringgit"},
            3
        );
        allQuestions[13] = new Question(
            "Which element is essential for human bones?",
            new String[]{"Iron", "Calcium", "Sodium", "Potassium"},
            2
        );
        allQuestions[14] = new Question(
            "What is the largest mammal in the world?",
            new String[]{"Elephant", "Blue Whale", "Giraffe", "Hippopotamus"},
            2
        );
        allQuestions[15] = new Question(
            "Which city is known as the 'Big Apple'?",
            new String[]{"Los Angeles", "Chicago", "New York City", "Miami"},
            3
        );
        allQuestions[16] = new Question(
            "What is the primary source of energy for Earth's climate system?",
            new String[]{"Wind", "Sun", "Geothermal Heat", "Ocean Currents"},
            2
        );
        allQuestions[17] = new Question(
            "Who was the first person to walk on the moon?",
            new String[]{"Buzz Aldrin", "Neil Armstrong", "Yuri Gagarin", "Alan Shepard"},
            2
        );
        allQuestions[18] = new Question(
            "What is the national flower of India?",
            new String[]{"Rose", "Lotus", "Sunflower", "Tulip"},
            2
        );
        allQuestions[19] = new Question(
            "Which continent is the Sahara Desert located in?",
            new String[]{"Asia", "Africa", "Australia", "South America"},
            2
        );
        allQuestions[20] = new Question(
            "What is the tallest mountain in the world?",
            new String[]{"K2", "Kangchenjunga", "Mount Everest", "Lhotse"},
            3
        );
        allQuestions[21] = new Question(
            "Which country is famous for the Eiffel Tower?",
            new String[]{"Italy", "France", "Spain", "Germany"},
            2
        );
        allQuestions[22] = new Question(
            "What gas do humans primarily exhale?",
            new String[]{"Oxygen", "Carbon Dioxide", "Nitrogen", "Helium"},
            2
        );
        allQuestions[23] = new Question(
            "Who invented the telephone?",
            new String[]{"Thomas Edison", "Alexander Graham Bell", "Nikola Tesla", "Guglielmo Marconi"},
            2
        );
        allQuestions[24] = new Question(
            "What is the capital of Australia?",
            new String[]{"Sydney", "Melbourne", "Canberra", "Perth"},
            3
        );
        allQuestions[25] = new Question(
            "Which organ pumps blood in the human body?",
            new String[]{"Lungs", "Heart", "Liver", "Kidneys"},
            2
        );
        allQuestions[26] = new Question(
            "What is the national animal of China?",
            new String[]{"Panda", "Tiger", "Dragon", "Elephant"},
            1
        );
        allQuestions[27] = new Question(
            "Which country is known as the 'Land of the Rising Sun'?",
            new String[]{"China", "Japan", "South Korea", "Thailand"},
            2
        );
        allQuestions[28] = new Question(
            "What is the boiling point of water in Celsius?",
            new String[]{"0 degrees", "50 degrees", "100 degrees", "150 degrees"},
            3
        );
        allQuestions[29] = new Question(
            "Which famous structure is located in Egypt?",
            new String[]{"Eiffel Tower", "Great Wall", "Pyramids of Giza", "Colosseum"},
            3
        );
        allQuestions[30] = new Question(
            "Which sport is associated with Wimbledon?",
            new String[]{"Football", "Tennis", "Cricket", "Golf"},
            2
        );
        allQuestions[31] = new Question(
            "What is the capital city of Brazil?",
            new String[]{"Rio de Janeiro", "Sao Paulo", "Brasilia", "Salvador"},
            3
        );
        allQuestions[32] = new Question(
            "Which scientist discovered penicillin?",
            new String[]{"Marie Curie", "Alexander Fleming", "Gregor Mendel", "Michael Faraday"},
            2
        );
        allQuestions[33] = new Question(
            "What is the largest planet in our solar system?",
            new String[]{"Earth", "Saturn", "Jupiter", "Uranus"},
            3
        );
        allQuestions[34] = new Question(
            "Which country is known for the Great Wall?",
            new String[]{"India", "China", "Japan", "Russia"},
            2
        );
        allQuestions[35] = new Question(
            "What is the currency of the United Kingdom?",
            new String[]{"Dollar", "Euro", "Pound", "Rupee"},
            3
        );
        allQuestions[36] = new Question(
            "Which gas makes up the majority of Earth's atmosphere?",
            new String[]{"Oxygen", "Carbon Dioxide", "Nitrogen", "Argon"},
            3
        );
        allQuestions[37] = new Question(
            "Who wrote the novel 'Pride and Prejudice'?",
            new String[]{"Jane Austen", "Charlotte Bronte", "Emily Dickinson", "Virginia Woolf"},
            1
        );
        allQuestions[38] = new Question(
            "What is the capital of Canada?",
            new String[]{"Toronto", "Vancouver", "Ottawa", "Montreal"},
            3
        );
        allQuestions[39] = new Question(
            "Which animal is the fastest land animal?",
            new String[]{"Lion", "Cheetah", "Horse", "Kangaroo"},
            2
        );
        allQuestions[40] = new Question(
            "What is the chemical symbol for water?",
            new String[]{"H2O", "CO2", "O2", "N2"},
            1
        );
        allQuestions[41] = new Question(
            "Which country is famous for the Taj Mahal?",
            new String[]{"Pakistan", "India", "Bangladesh", "Nepal"},
            2
        );
        allQuestions[42] = new Question(
            "What is the freezing point of water in Celsius?",
            new String[]{"0 degrees", "32 degrees", "100 degrees", "-10 degrees"},
            1
        );
        allQuestions[43] = new Question(
            "Who was the first president of the United States?",
            new String[]{"Thomas Jefferson", "George Washington", "Abraham Lincoln", "John Adams"},
            2
        );
        allQuestions[44] = new Question(
            "What is the national bird of the United States?",
            new String[]{"Bald Eagle", "Peacock", "Flamingo", "Owl"},
            1
        );
        allQuestions[45] = new Question(
            "Which continent is known as the 'Dark Continent'?",
            new String[]{"Asia", "Africa", "Europe", "South America"},
            2
        );
        allQuestions[46] = new Question(
            "What is the capital of Russia?",
            new String[]{"St. Petersburg", "Moscow", "Kiev", "Minsk"},
            2
        );
        allQuestions[47] = new Question(
            "Which element is used in pencils?",
            new String[]{"Carbon", "Graphite", "Lead", "Charcoal"},
            2
        );
        allQuestions[48] = new Question(
            "What is the national sport of Japan?",
            new String[]{"Baseball", "Sumo Wrestling", "Soccer", "Karate"},
            2
        );
        allQuestions[49] = new Question(
            "Which famous landmark is located in New York Harbor?",
            new String[]{"Golden Gate Bridge", "Statue of Liberty", "Mount Rushmore", "Empire State Building"},
            2
        );

        List<Question> list = Arrays.asList(allQuestions);
        Collections.shuffle(list);
        return list.subList(0, QUESTIONS_PER_ROUND).toArray(new Question[0]);
    }

    private void loadQuestion(int index) {
        if (index < questions.length) {
            Question q = questions[index];
            questionArea.setText("Q" + (index + 1) + ": " + q.getQuestion());
            String[] opts = q.getOptions();
            List<String> shuffledOptions = new ArrayList<>(Arrays.asList(opts));
            Collections.shuffle(shuffledOptions);
            for (int i = 0; i < opts.length; i++) {
                options[i].setText(shuffledOptions.get(i));
                options[i].setSelected(false);
            }
            optionGroup.clearSelection();
            int targetValue = index + 1;
            Timer progressTimer = new Timer(50, e -> {
                int currentValue = progressBar.getValue();
                if (currentValue < targetValue) {
                    progressBar.setValue(currentValue + 1);
                    progressBar.setString("Progress: " + (currentValue + 1) + "/" + QUESTIONS_PER_ROUND);
                } else {
                    ((Timer)e.getSource()).stop();
                }
            });
            progressTimer.start();
            fadeOpacity = 0.0f;
            if (fadeTimer != null) fadeTimer.stop();
            fadeTimer = new Timer(50, e -> {
                fadeOpacity += 0.1f;
                if (fadeOpacity >= 1.0f) {
                    fadeOpacity = 1.0f;
                    ((Timer)e.getSource()).stop();
                }
                getContentPane().repaint();
            });
            fadeTimer.start();
            startTimer();
        }
    }

    private int getSelectedOption() {
        for (int i = 0; i < options.length; i++) {
            if (options[i].isSelected()) {
                return i + 1;
            }
        }
        return 0;
    }

    private void startTimer() {
        timeLeft = 15;
        timerLabel.setText("Time Left: " + timeLeft + "s");
        if (timer != null) timer.stop();

        timer = new Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText("Time Left: " + timeLeft + "s");
            if (timeLeft <= 0) {
                timer.stop();
                autoSubmit();
            }
        });
        timer.start();
    }

    private void autoSubmit() {
        JOptionPane.showMessageDialog(this, "Time's up for this question!");
        processAnswer();
        goNextOrFinish();
    }

    private void processAnswer() {
        int selected = getSelectedOption();
        if (selected != 0) {
            String selectedOption = options[selected - 1].getText();
            String[] originalOptions = questions[current].getOptions();
            int correctIndex = questions[current].getCorrectOption() - 1;
            if (selectedOption.equals(originalOptions[correctIndex])) {
                score++;
            }
        }
        // Debug: System.out.println("Question " + (current + 1) + " processed. Selected: " + selected + ", Score: " + score);
    }

    private void goNextOrFinish() {
        current++;
        // Debug: System.out.println("goNextOrFinish called. Current: " + current + ", Total Questions: " + questions.length);
        if (current < questions.length) {
            loadQuestion(current);
        } else {
            nextButton.setEnabled(false);
            submitButton.setEnabled(true);
            // Debug: System.out.println("Reached end of questions. Next disabled, Submit enabled.");
            // Removed the intermediate dialog to prevent stacking
        }
    }

    private void showResult() {
        // Stop all timers to prevent UI interference
        if (timer != null) timer.stop();
        if (fadeTimer != null) fadeTimer.stop();
        if (buttonScaleTimer != null) buttonScaleTimer.stop();

        // Debug: System.out.println("showResult called. Final Score: " + score + " out of " + questions.length);

        String resultMessage = "Quiz Completed!\nYour Score: " + score + " / " + questions.length;
        try {
            SwingUtilities.invokeLater(() -> {
                // Debug: System.out.println("Displaying result dialog with message: " + resultMessage);
                JOptionPane.showMessageDialog(this, resultMessage, "Result", JOptionPane.INFORMATION_MESSAGE);
                // Add a confirmation before exiting
                int confirm = JOptionPane.showConfirmDialog(this, "Do you want to exit the quiz?", "Exit", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            });
        } catch (Exception ex) {
            // Debug: System.out.println("Error displaying result dialog: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Error displaying result: " + score + " / " + questions.length, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        toggleThemeButton.setText(isDarkMode ? "Light Mode" : "Dark Mode");
        applyTheme();
    }

    private void applyTheme() {
        Color bg = isDarkMode ? darkBg : lightBg;
        Color fg = isDarkMode ? darkFg : lightFg;

        getContentPane().setBackground(bg);
        questionArea.setForeground(fg);
        questionArea.setBackground(bg);
        timerLabel.setForeground(isDarkMode ? new Color(255, 182, 193) : new Color(200, 0, 0));

        for (JRadioButton option : options) {
            option.setBackground(bg);
            option.setForeground(fg);
        }

        styleButton(nextButton);
        styleButton(submitButton);
        styleButton(toggleThemeButton);
        progressBar.setBackground(bg);
        progressBar.setForeground(accentColor);

        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JPanel) {
                ((JPanel) comp).setBackground(bg);
                comp.repaint();
            }
        }
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == nextButton) {
            // Debug: System.out.println("Next button clicked. Current: " + current);
            int selected = getSelectedOption();
            if (selected == 0) {
                JOptionPane.showMessageDialog(this, "Please select an option before proceeding!");
                return;
            }
            if (timer != null) timer.stop();
            processAnswer();
            goNextOrFinish();
        } else if (e.getSource() == submitButton) {
            // Debug: System.out.println("Submit button clicked. Current: " + current);
            processAnswer(); // Process the last question regardless of selection
            showResult();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(QuizFrame::new);
    }
}