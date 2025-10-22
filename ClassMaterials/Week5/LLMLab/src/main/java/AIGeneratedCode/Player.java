package AIGeneratedCode;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private final List<ChessPiece> pile;

    public Player() {
        this.pile = new ArrayList<>();
    }

    public Player(List<ChessPiece> pile) {
        this.pile = pile;
    }

    public List<ChessPiece> getPile() {
        return pile;
    }
}
