---
marp: true
theme: default
paginate: true
---

# Java Swing Programming

---

## Slide 1: Introduction to Java Swing

- Java Swing is a part of Java Foundation Classes (JFC).
- Used for building graphical user interfaces (GUIs).
- Provides a rich set of widgets and controls.

---

## Slide 2: Swing vs AWT

- AWT (Abstract Window Toolkit) is the predecessor of Swing.
- Swing is more flexible and provides more components.
- Swing components are lightweight compared to AWT.

---

## Slide 3: Setting Up Swing

- Import necessary packages:
  ```java
  import javax.swing.*;
  import java.awt.*;
  import java.awt.event.*;
  ```
- Create a JFrame:
  ```java
  JFrame frame = new JFrame("My Swing Application");
  frame.setSize(400, 300);
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  ```

---

## Slide 4: Adding Components

- Add components to the frame:
  ```java
  JButton button = new JButton("Click Me");
  frame.add(button);
  frame.setVisible(true);
  ```

---

## Slide 5: Layout Managers

- Layout managers control the positioning of components.
- Common layout managers:
  - BorderLayout (default for JFrame)
  - FlowLayout (default for JPanel)
  - GridLayout
  - GridBagLayout
  - BoxLayout

---

## Slide 6: BorderLayout

- Divides the container into five regions: North, South, East, West, Center.
  ```java
  frame.setLayout(new BorderLayout());
  frame.add(button, BorderLayout.NORTH);
  frame.pack();
  ```

---

## Slide 7: FlowLayout

- Arranges components in a left-to-right flow.
  ```java
  JPanel panel = new JPanel(new FlowLayout());
  panel.add(button);
  frame.add(panel);
  ```

---

## Slide 8: GridLayout

- Arranges components in a grid of cells.
  ```java
  frame.setLayout(new GridLayout(2, 2, 5, 5));  // rows, cols, hgap, vgap
  frame.add(button);
  ```

---

## Slide 9: Event Handling

- Handle events using ActionListener:
  ```java
  button.addActionListener(e -> {
      System.out.println("Button clicked!");
  });
  ```

---

## Slide 10: JTextField

- Single-line text input:
  ```java
  JTextField textField = new JTextField(20);
  frame.add(new JLabel("Enter text: "), BorderLayout.WEST);
  frame.add(textField, BorderLayout.CENTER);
  ```

---

## Slide 11: JTextArea

- Multi-line text input:
  ```java
  JTextArea textArea = new JTextArea(5, 20);
  textArea.setLineWrap(true);
  textArea.setWrapStyleWord(true);
  frame.add(new JScrollPane(textArea));
  ```

---

## Slide 12: JCheckBox

- Checkbox component:
  ```java
  JCheckBox checkBox = new JCheckBox("Accept Terms");
  checkBox.setSelected(false);
  frame.add(checkBox);
  ```

---

## Slide 13: JRadioButton

- Radio button component:
  ```java
  ButtonGroup group = new ButtonGroup();
  JRadioButton radio1 = new JRadioButton("Option 1", true);
  JRadioButton radio2 = new JRadioButton("Option 2");
  group.add(radio1);
  group.add(radio2);
  ```

---

## Slide 14: ButtonGroup

- Group radio buttons:
  ```java
  JPanel radioPanel = new JPanel();
  radioPanel.add(radio1);
  radioPanel.add(radio2);
  frame.add(radioPanel);
  ```

---

## Slide 15: JComboBox

- Drop-down list:
  ```java
  String[] items = {"Item 1", "Item 2", "Item 3"};
  JComboBox<String> comboBox = new JComboBox<>(items);
  comboBox.setSelectedIndex(0);
  frame.add(comboBox);
  ```

---

## Slide 16: JList

- List component:
  ```java
  String[] items = {"Item 1", "Item 2", "Item 3"};
  JList<String> list = new JList<>(items);
  list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
  frame.add(new JScrollPane(list));
  ```

---

## Slide 17: JScrollPane

- Scrollable view:
  ```java
  JScrollPane scrollPane = new JScrollPane(textArea);
  scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
  frame.add(scrollPane);
  ```

---

## Slide 18: JMenuBar

- Menu bar:
  ```java
  JMenuBar menuBar = new JMenuBar();
  JMenu fileMenu = new JMenu("File");
  JMenu editMenu = new JMenu("Edit");
  menuBar.add(fileMenu);
  menuBar.add(editMenu);
  frame.setJMenuBar(menuBar);
  ```

---

## Slide 19: JMenuItem

- Menu items:
  ```java
  JMenuItem openItem = new JMenuItem("Open");
  JMenuItem saveItem = new JMenuItem("Save");
  fileMenu.add(openItem);
  fileMenu.add(saveItem);
  fileMenu.addSeparator();
  fileMenu.add(new JMenuItem("Exit"));
  ```

---

## Slide 20: Conclusion

- Java Swing provides a comprehensive set of GUI components
- Uses event-driven programming model
- Best practices:
  - Always call Swing components on Event Dispatch Thread
  - Use layouts for responsive design
  - Add proper event handling
  - Include input validation

---

# Thank You!

- Questions?
- Discussion
