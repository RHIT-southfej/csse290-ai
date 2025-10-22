package api;

import java.util.List;

import AIGeneratedCode.ChessPiece;
import AIGeneratedCode.Player;

/**
 * Interface from the UML. The concrete AIGeneratedCode.BusinessLogic implements this
 * and provides accessors that surface the current game state.
 */
public interface BusinessLogic {
    List<Player> getPlayers();
    List<ChessPiece> getDiscard();
    List<ChessPiece> getUnowned();
}
