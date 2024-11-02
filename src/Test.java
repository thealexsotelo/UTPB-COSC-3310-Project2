import javafx.scene.text.Font;

public class Test {
    public static void main(String[] args) {
        for (String s : Font.getFontNames()) {
            System.out.println(s);
        }
    }
}
