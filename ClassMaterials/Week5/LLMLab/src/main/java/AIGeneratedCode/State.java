package AIGeneratedCode;

import java.util.ArrayList;
import java.util.List;

public class State {
    private final List<Player> players = new ArrayList<>();
    private final List<ChessPiece> discard = new ArrayList<>();
    private final List<ChessPiece> unowned = new ArrayList<>();

    public List<Player> getPlayers() {
        return players;
    }

    public List<ChessPiece> getDiscard() {
        return discard;
    }

    public List<ChessPiece> getUnowned() {
        return unowned;
    }
}
