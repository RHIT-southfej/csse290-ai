import static org.junit.jupiter.api.Assertions.*;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import AIGeneratedCode.BusinessLogic;
import AIGeneratedCode.ChessPiece;
import AIGeneratedCode.Color;
import AIGeneratedCode.PieceType;
import AIGeneratedCode.Player;
import AIGeneratedCode.State;

public class JUnitTestCase {

	@Test
	void testInitialStateOfGame() {
		// Arrange: create the system under test per the diagram
		BusinessLogic bl = new BusinessLogic();

		// We are going to assert about the returned State object
		State state = bl.getState();

		// Assert: there are four players and each has five pieces in their pile
		List<Player> players = state.getPlayers();
		assertEquals(4, players.size(), "Expected four players at start");
		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			assertEquals(5, p.getPile().size(), "Player " + i + " should start with five pieces");
		}

		// Assert: discard has exactly one non-null piece
		List<ChessPiece> discard = state.getDiscard();
		assertEquals(1, discard.size(), "Discard should contain one piece");
		assertNotNull(discard.get(0), "Discarded piece should not be null");

		// Assert: unowned has the remainder of the 64-piece set
		// Stage 3: Now 80 total pieces (64 + 16 rainbow)
		int expectedUnowned = 80 - (4 * 5) - 1; // total minus players' piles minus discard
		assertEquals(expectedUnowned, state.getUnowned().size(), "Unowned should contain the remaining pieces");

		// Assert: across all player piles, each color has the standard piece counts
		// 8 pawns, 2 rooks, 2 knights, 2 bishops, 1 queen, 1 king
		// Note: Stage 3 added RAINBOW color, but this Stage 1 test only checks original colors
		for (Color color : Color.values()) {
			// Skip RAINBOW for Stage 1 test - it's tested separately in Stage 3
			if (color == Color.RAINBOW) {
				continue;
			}
			
			Map<PieceType, Integer> counts = new EnumMap<>(PieceType.class);
			for (PieceType t : PieceType.values()) {
				counts.put(t, 0);
			}

			players.stream()
				.flatMap(p -> p.getPile().stream())
				.filter(piece -> piece.getColor() == color)
				.forEach(piece -> counts.put(piece.getType(), counts.get(piece.getType()) + 1));

			assertEquals(8, counts.get(PieceType.PAWN).intValue(), "Each color should have 8 pawns across piles");
			assertEquals(2, counts.get(PieceType.ROOK).intValue(), "Each color should have 2 rooks across piles");
			assertEquals(2, counts.get(PieceType.KNIGHT).intValue(), "Each color should have 2 knights across piles");
			assertEquals(2, counts.get(PieceType.BISHOP).intValue(), "Each color should have 2 bishops across piles");
			assertEquals(1, counts.get(PieceType.QUEEN).intValue(), "Each color should have 1 queen across piles");
			assertEquals(1, counts.get(PieceType.KING).intValue(), "Each color should have 1 king across piles");
		}
	}

	@Test
	void testCanDiscardMatchingColor() {
		// Arrange: create a game and get the top discard piece
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		ChessPiece topDiscard = state.getDiscard().get(state.getDiscard().size() - 1);
		
		// Create a piece that matches the color of the discard
		ChessPiece matchingColorPiece = new ChessPiece(topDiscard.getColor(), PieceType.BISHOP);
		
		// Act & Assert: should be able to discard a piece with matching color
		assertTrue(bl.canDiscard(matchingColorPiece), "Should be able to discard piece with matching color");
	}

	@Test
	void testCanDiscardMatchingType() {
		// Arrange: create a game and get the top discard piece
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		ChessPiece topDiscard = state.getDiscard().get(state.getDiscard().size() - 1);
		
		// Create a piece that matches the type but different color (if possible)
		Color differentColor = (topDiscard.getColor() == Color.WHITE) ? null : Color.WHITE;
		ChessPiece matchingTypePiece = new ChessPiece(differentColor, topDiscard.getType());
		
		// Act & Assert: should be able to discard a piece with matching type
		assertTrue(bl.canDiscard(matchingTypePiece), "Should be able to discard piece with matching type");
	}

	@Test
	void testCannotDiscardNonMatchingPiece() {
		// Arrange: create a game with a known discard state
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		ChessPiece topDiscard = state.getDiscard().get(state.getDiscard().size() - 1);
		
		// Find a piece that doesn't match color or type
		PieceType differentType = (topDiscard.getType() == PieceType.PAWN) ? PieceType.ROOK : PieceType.PAWN;
		Color differentColor = null; // Use null since we only have WHITE
		ChessPiece nonMatchingPiece = new ChessPiece(differentColor, differentType);
		
		// Act & Assert: should NOT be able to discard a non-matching piece
		assertFalse(bl.canDiscard(nonMatchingPiece), "Should not be able to discard piece that doesn't match color or type");
	}

	@Test
	void testDiscardValidPiece() {
		// Arrange: set up a game with a known state
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		Player currentPlayer = bl.getCurrentPlayer();
		ChessPiece topDiscard = state.getDiscard().get(state.getDiscard().size() - 1);
		
		// Find a valid piece to discard from current player's pile
		ChessPiece pieceToDiscard = null;
		for (ChessPiece piece : currentPlayer.getPile()) {
			if (bl.canDiscard(piece)) {
				pieceToDiscard = piece;
				break;
			}
		}
		
		// If no valid piece found, add one
		if (pieceToDiscard == null) {
			pieceToDiscard = new ChessPiece(topDiscard.getColor(), topDiscard.getType());
			currentPlayer.getPile().add(pieceToDiscard);
		}
		
		int initialPileSize = currentPlayer.getPile().size();
		int initialDiscardSize = state.getDiscard().size();
		
		// Act: discard the piece
		boolean result = bl.discard(pieceToDiscard);
		
		// Assert: piece should be moved from player pile to discard
		assertTrue(result, "Discard should succeed for valid piece");
		assertEquals(initialPileSize - 1, currentPlayer.getPile().size(), "Player pile should decrease by 1");
		assertEquals(initialDiscardSize + 1, state.getDiscard().size(), "Discard pile should increase by 1");
		assertEquals(pieceToDiscard, state.getDiscard().get(state.getDiscard().size() - 1), "Discarded piece should be on top");
	}

	@Test
	void testDrawFromUnowned() {
		// Arrange: create a game
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		Player currentPlayer = bl.getCurrentPlayer();
		
		int initialPileSize = currentPlayer.getPile().size();
		int initialUnownedSize = state.getUnowned().size();
		
		// Act: draw a piece
		boolean result = bl.draw();
		
		// Assert: piece should move from unowned to player pile
		assertTrue(result, "Draw should succeed when unowned pile has pieces");
		assertEquals(initialPileSize + 1, currentPlayer.getPile().size(), "Player pile should increase by 1");
		assertEquals(initialUnownedSize - 1, state.getUnowned().size(), "Unowned pile should decrease by 1");
	}

	@Test
	void testCannotDrawFromEmptyUnowned() {
		// Arrange: create a game and empty the unowned pile
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		state.getUnowned().clear();
		
		// Act: attempt to draw
		boolean result = bl.draw();
		
		// Assert: draw should fail
		assertFalse(result, "Draw should fail when unowned pile is empty");
	}

	@Test
	void testForfeit() {
		// Arrange: create a game
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		
		int initialPlayerCount = state.getPlayers().size();
		Player currentPlayer = bl.getCurrentPlayer();
		
		// Act: current player forfeits
		bl.forfeit();
		
		// Assert: player should be removed from rotation
		assertEquals(initialPlayerCount - 1, state.getPlayers().size(), "Player count should decrease by 1");
		assertFalse(state.getPlayers().contains(currentPlayer), "Forfeited player should be removed");
	}

	@Test
	void testWinByEmptyingPile() {
		// Arrange: create a game and set up a player to win
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		Player currentPlayer = bl.getCurrentPlayer();
		
		// Give player just one piece that can be discarded
		currentPlayer.getPile().clear();
		ChessPiece topDiscard = state.getDiscard().get(state.getDiscard().size() - 1);
		ChessPiece lastPiece = new ChessPiece(topDiscard.getColor(), topDiscard.getType());
		currentPlayer.getPile().add(lastPiece);
		
		// Act: discard the last piece
		bl.discard(lastPiece);
		
		// Assert: game should be over and current player should win
		assertTrue(bl.isGameOver(), "Game should be over when player empties their pile");
		assertEquals(currentPlayer, bl.getWinner(), "Player who emptied pile should win");
	}

	@Test
	void testWinByLastPlayerRemaining() {
		// Arrange: create a game with only 2 players left
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		
		// Remove all but 2 players
		while (state.getPlayers().size() > 2) {
			state.getPlayers().remove(state.getPlayers().size() - 1);
		}
		
		// Act: one player forfeits
		bl.forfeit();
		
		// Assert: game should be over and remaining player should win
		assertTrue(bl.isGameOver(), "Game should be over when only one player remains");
		assertNotNull(bl.getWinner(), "There should be a winner");
		assertEquals(1, state.getPlayers().size(), "Only one player should remain");
	}

	@Test
	void testCurrentPlayerRotation() {
		// Arrange: create a game
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		
		Player firstPlayer = bl.getCurrentPlayer();
		
		// Act: draw (which advances turn if successful)
		bl.draw();
		Player secondPlayer = bl.getCurrentPlayer();
		
		// Assert: current player should change after an action
		assertNotEquals(firstPlayer, secondPlayer, "Current player should rotate after turn");
	}

	// ==================== Stage 3 Tests ====================

	@Test
	void testRainbowPieceCanAlwaysBeDiscarded() {
		// Arrange: create a game and ensure discard pile has a non-rainbow piece
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		
		// Clear discard and add a non-rainbow piece
		state.getDiscard().clear();
		state.getDiscard().add(new ChessPiece(Color.WHITE, PieceType.PAWN));
		
		// Create a rainbow piece
		ChessPiece rainbowPiece = new ChessPiece(Color.RAINBOW, PieceType.QUEEN);
		
		// Act & Assert: rainbow piece should always be discardable
		assertTrue(bl.canDiscard(rainbowPiece), "Rainbow pieces should always be discardable");
	}

	@Test
	void testRainbowDiscardReversesOrder() {
		// Arrange: create a game
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		
		// Get initial player order
		Player firstPlayer = bl.getCurrentPlayer();
		
		// Add a rainbow piece to current player's pile
		ChessPiece rainbowPiece = new ChessPiece(Color.RAINBOW, PieceType.KNIGHT);
		firstPlayer.getPile().add(rainbowPiece);
		
		// Draw to advance to next player (normal forward direction)
		bl.draw();
		Player secondPlayer = bl.getCurrentPlayer();
		
		// Add another rainbow piece and discard it
		rainbowPiece = new ChessPiece(Color.RAINBOW, PieceType.BISHOP);
		secondPlayer.getPile().add(rainbowPiece);
		bl.discard(rainbowPiece);
		
		// After discarding rainbow, direction should reverse
		// So next player should be back to first player (going backwards)
		Player thirdPlayer = bl.getCurrentPlayer();
		
		// Assert: after rainbow discard and reversal, we should go backwards
		assertEquals(firstPlayer, thirdPlayer, "After rainbow discard, turn order should reverse");
	}

	@Test
	void testRainbowIsOwnColor() {
		// Arrange: create a game with rainbow on discard
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		
		// Clear discard and add a rainbow piece
		state.getDiscard().clear();
		state.getDiscard().add(new ChessPiece(Color.RAINBOW, PieceType.PAWN));
		
		// Act & Assert: another rainbow piece should match the color
		ChessPiece anotherRainbow = new ChessPiece(Color.RAINBOW, PieceType.ROOK);
		assertTrue(bl.canDiscard(anotherRainbow), "Rainbow should match rainbow color");
		
		// But a non-rainbow piece should only match if type matches
		ChessPiece whiteRook = new ChessPiece(Color.WHITE, PieceType.ROOK);
		assertFalse(bl.canDiscard(whiteRook), "Non-rainbow should not match rainbow color (different types)");
		
		ChessPiece whitePawn = new ChessPiece(Color.WHITE, PieceType.PAWN);
		assertTrue(bl.canDiscard(whitePawn), "Non-rainbow should match rainbow if type matches");
	}

	@Test
	void testInitialStateWithRainbowPieces() {
		// Arrange & Act: create a game
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		
		// Count total pieces
		int totalPieces = 0;
		for (Player p : state.getPlayers()) {
			totalPieces += p.getPile().size();
		}
		totalPieces += state.getDiscard().size();
		totalPieces += state.getUnowned().size();
		
		// Assert: should have 80 total pieces (64 original + 16 rainbow)
		assertEquals(80, totalPieces, "Total pieces should be 80 with rainbow pieces added");
		
		// Count rainbow pieces across all locations
		long rainbowCount = state.getPlayers().stream()
			.flatMap(p -> p.getPile().stream())
			.filter(piece -> piece.getColor() == Color.RAINBOW)
			.count();
		rainbowCount += state.getDiscard().stream()
			.filter(piece -> piece.getColor() == Color.RAINBOW)
			.count();
		rainbowCount += state.getUnowned().stream()
			.filter(piece -> piece.getColor() == Color.RAINBOW)
			.count();
		
		// Assert: should have 16 rainbow pieces (one full set)
		assertEquals(16, rainbowCount, "Should have 16 rainbow pieces in the game");
	}

	@Test
	void testMultipleRainbowDiscards() {
		// Arrange: create a game
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		
		Player p0 = state.getPlayers().get(0);
		Player p1 = state.getPlayers().get(1);
		Player p2 = state.getPlayers().get(2);
		
		// Start at p0
		while (bl.getCurrentPlayer() != p0) {
			bl.draw();
		}
		
		// Discard first rainbow piece from p0 (reverses direction)
		ChessPiece rainbow1 = new ChessPiece(Color.RAINBOW, PieceType.PAWN);
		p0.getPile().add(rainbow1);
		bl.discard(rainbow1);
		
		// After reverse, should go backwards to p3 (last player)
		Player afterFirstReverse = bl.getCurrentPlayer();
		assertEquals(state.getPlayers().get(3), afterFirstReverse, "After first reverse, should go backwards");
		
		// Discard second rainbow piece (reverses direction back to normal)
		ChessPiece rainbow2 = new ChessPiece(Color.RAINBOW, PieceType.KNIGHT);
		afterFirstReverse.getPile().add(rainbow2);
		bl.discard(rainbow2);
		
		// After two reversals, we should be going forward again
		// From p3 forward should go to p0
		Player afterSecondReverse = bl.getCurrentPlayer();
		assertEquals(p0, afterSecondReverse, "After two reversals, should go forward: p3 -> p0");
	}

	@Test
	void testTurnOrderReversalPersists() {
		// Arrange: create a game and reverse the order
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		
		Player p0 = state.getPlayers().get(0);
		Player p1 = state.getPlayers().get(1);
		Player p2 = state.getPlayers().get(2);
		Player p3 = state.getPlayers().get(3);
		
		// Set current to p1
		while (bl.getCurrentPlayer() != p1) {
			bl.draw();
		}
		
		// Discard rainbow from p1 to reverse
		ChessPiece rainbow = new ChessPiece(Color.RAINBOW, PieceType.QUEEN);
		p1.getPile().add(rainbow);
		bl.discard(rainbow);
		
		// Now should go backwards: p1 -> p0
		assertEquals(p0, bl.getCurrentPlayer(), "After reverse from p1, should go to p0");
		
		// Draw to continue backwards: p0 -> p3
		bl.draw();
		assertEquals(p3, bl.getCurrentPlayer(), "After p0's turn, should go to p3 (backwards)");
		
		// Draw to continue backwards: p3 -> p2
		bl.draw();
		assertEquals(p2, bl.getCurrentPlayer(), "After p3's turn, should go to p2 (backwards)");
	}

	// ==================== Stage 4 Tests ====================

	@Test
	void testMonopolyDetectionWithFivePieces() {
		// Arrange: create a game and give a player 5+ pieces of same color
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		Player p0 = state.getPlayers().get(0);
		
		// Clear pile and add 5 WHITE pieces
		p0.getPile().clear();
		for (int i = 0; i < 5; i++) {
			p0.getPile().add(new ChessPiece(Color.WHITE, PieceType.PAWN));
		}
		
		// Act & Assert: should have monopoly on WHITE
		assertTrue(bl.hasMonopoly(p0), "Player with 5 pieces of same color should have monopoly");
		assertEquals(Color.WHITE, bl.getMonopolyColor(p0), "Monopoly should be on WHITE color");
	}

	@Test
	void testNoMonopolyWithLessThanFivePieces() {
		// Arrange: create a game
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		Player p0 = state.getPlayers().get(0);
		
		// Clear pile and add only 4 WHITE pieces
		p0.getPile().clear();
		for (int i = 0; i < 4; i++) {
			p0.getPile().add(new ChessPiece(Color.WHITE, PieceType.PAWN));
		}
		
		// Act & Assert: should NOT have monopoly
		assertFalse(bl.hasMonopoly(p0), "Player with less than 5 pieces should not have monopoly");
	}

	@Test
	void testMonopolyRequiresMostOfColor() {
		// Arrange: create a game with two players having same color
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		Player p0 = state.getPlayers().get(0);
		Player p1 = state.getPlayers().get(1);
		
		// P0 has 5 WHITE pieces
		p0.getPile().clear();
		for (int i = 0; i < 5; i++) {
			p0.getPile().add(new ChessPiece(Color.WHITE, PieceType.PAWN));
		}
		
		// P1 has 6 WHITE pieces (more than P0)
		p1.getPile().clear();
		for (int i = 0; i < 6; i++) {
			p1.getPile().add(new ChessPiece(Color.WHITE, PieceType.PAWN));
		}
		
		// Act & Assert: P0 should NOT have monopoly, P1 should
		assertFalse(bl.hasMonopoly(p0), "Player with 5 pieces should not have monopoly if another has more");
		assertTrue(bl.hasMonopoly(p1), "Player with most pieces (6) should have monopoly");
		assertEquals(Color.WHITE, bl.getMonopolyColor(p1), "P1's monopoly should be on WHITE");
	}

	@Test
	void testSingleMonopolyOnMostColor() {
		// Arrange: player with multiple colors, most pieces in one color
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		Player p0 = state.getPlayers().get(0);
		
		// P0 has 6 WHITE, 3 RAINBOW
		p0.getPile().clear();
		for (int i = 0; i < 6; i++) {
			p0.getPile().add(new ChessPiece(Color.WHITE, PieceType.PAWN));
		}
		for (int i = 0; i < 3; i++) {
			p0.getPile().add(new ChessPiece(Color.RAINBOW, PieceType.PAWN));
		}
		
		// Act & Assert: monopoly should be on WHITE (most pieces)
		assertTrue(bl.hasMonopoly(p0), "Player should have monopoly");
		assertEquals(Color.WHITE, bl.getMonopolyColor(p0), "Monopoly should be on color with most pieces (WHITE)");
	}

	@Test
	void testCanDrawMonopolyPawn() {
		// Arrange: set up player with monopoly and monopoly pawns in unowned
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		Player p0 = state.getPlayers().get(0);
		
		// Give P0 monopoly on WHITE (5+ pieces, most of that color)
		p0.getPile().clear();
		for (int i = 0; i < 5; i++) {
			p0.getPile().add(new ChessPiece(Color.WHITE, PieceType.ROOK));
		}
		
		// Ensure there's a WHITE pawn in unowned pile
		state.getUnowned().clear();
		state.getUnowned().add(new ChessPiece(Color.WHITE, PieceType.PAWN));
		
		// Set P0 as current player
		while (bl.getCurrentPlayer() != p0) {
			bl.draw();
			if (state.getUnowned().isEmpty()) {
				state.getUnowned().add(new ChessPiece(Color.WHITE, PieceType.PAWN));
			}
		}
		
		// Act & Assert: should be able to draw monopoly pawn
		assertTrue(bl.canDrawMonopolyPawn(), "Player with monopoly should be able to draw monopoly pawn if available");
	}

	@Test
	void testDrawMonopolyPawn() {
		// Arrange: set up player with monopoly
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		Player p0 = state.getPlayers().get(0);
		
		// Give P0 monopoly on WHITE
		p0.getPile().clear();
		for (int i = 0; i < 5; i++) {
			p0.getPile().add(new ChessPiece(Color.WHITE, PieceType.ROOK));
		}
		
		// Add WHITE pawns to unowned
		state.getUnowned().clear();
		ChessPiece whitePawn = new ChessPiece(Color.WHITE, PieceType.PAWN);
		state.getUnowned().add(whitePawn);
		
		// Set P0 as current player
		while (bl.getCurrentPlayer() != p0) {
			bl.draw();
			if (state.getUnowned().isEmpty()) {
				state.getUnowned().add(new ChessPiece(Color.WHITE, PieceType.PAWN));
			}
		}
		
		int initialPileSize = p0.getPile().size();
		
		// Act: draw monopoly pawn
		boolean result = bl.drawMonopolyPawn();
		
		// Assert: pawn should be drawn
		assertTrue(result, "Should successfully draw monopoly pawn");
		assertEquals(initialPileSize + 1, p0.getPile().size(), "Pile should increase by 1");
		assertTrue(p0.getPile().stream().anyMatch(p -> p.getColor() == Color.WHITE && p.getType() == PieceType.PAWN), 
			"Should have drawn a WHITE pawn");
	}

	@Test
	void testCannotDrawMonopolyPawnIfNoneAvailable() {
		// Arrange: player with monopoly but no pawns of that color in unowned
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		Player p0 = state.getPlayers().get(0);
		
		// Give P0 monopoly on WHITE
		p0.getPile().clear();
		for (int i = 0; i < 5; i++) {
			p0.getPile().add(new ChessPiece(Color.WHITE, PieceType.ROOK));
		}
		
		// Unowned has only RAINBOW pieces, no WHITE pawns
		state.getUnowned().clear();
		state.getUnowned().add(new ChessPiece(Color.RAINBOW, PieceType.PAWN));
		
		// Set P0 as current player
		while (bl.getCurrentPlayer() != p0) {
			bl.draw();
			if (state.getUnowned().isEmpty()) {
				state.getUnowned().add(new ChessPiece(Color.RAINBOW, PieceType.PAWN));
			}
		}
		
		// Act & Assert: should NOT be able to draw monopoly pawn
		assertFalse(bl.canDrawMonopolyPawn(), "Should not be able to draw monopoly pawn if none available");
	}

	@Test
	void testMonopolyLossWhenAnotherPlayerGetsMore() {
		// Arrange: P0 has monopoly, then P1 gets more pieces of that color
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		Player p0 = state.getPlayers().get(0);
		Player p1 = state.getPlayers().get(1);
		
		// Initially P0 has monopoly with 5 WHITE
		p0.getPile().clear();
		for (int i = 0; i < 5; i++) {
			p0.getPile().add(new ChessPiece(Color.WHITE, PieceType.PAWN));
		}
		
		// P1 has 4 WHITE
		p1.getPile().clear();
		for (int i = 0; i < 4; i++) {
			p1.getPile().add(new ChessPiece(Color.WHITE, PieceType.PAWN));
		}
		
		// Verify P0 has monopoly
		assertTrue(bl.hasMonopoly(p0), "P0 should initially have monopoly");
		assertFalse(bl.hasMonopoly(p1), "P1 should not have monopoly yet");
		
		// P1 gains 2 more WHITE pieces (now has 6, more than P0's 5)
		p1.getPile().add(new ChessPiece(Color.WHITE, PieceType.PAWN));
		p1.getPile().add(new ChessPiece(Color.WHITE, PieceType.PAWN));
		
		// Act & Assert: monopoly should transfer to P1
		assertFalse(bl.hasMonopoly(p0), "P0 should lose monopoly when P1 has more");
		assertTrue(bl.hasMonopoly(p1), "P1 should gain monopoly with most WHITE pieces");
	}

	@Test
	void testMonopolyDrawAsAlternativeAction() {
		// Arrange: player with monopoly who cannot discard
		BusinessLogic bl = new BusinessLogic();
		State state = bl.getState();
		Player p0 = state.getPlayers().get(0);
		
		// Give P0 monopoly on RAINBOW
		p0.getPile().clear();
		for (int i = 0; i < 5; i++) {
			p0.getPile().add(new ChessPiece(Color.RAINBOW, PieceType.ROOK));
		}
		
		// Set discard to WHITE KING (P0 has no matching pieces)
		state.getDiscard().clear();
		state.getDiscard().add(new ChessPiece(Color.WHITE, PieceType.KING));
		
		// Add RAINBOW pawns to unowned
		state.getUnowned().clear();
		state.getUnowned().add(new ChessPiece(Color.RAINBOW, PieceType.PAWN));
		
		// Set P0 as current player
		while (bl.getCurrentPlayer() != p0) {
			bl.draw();
			if (state.getUnowned().isEmpty()) {
				state.getUnowned().add(new ChessPiece(Color.RAINBOW, PieceType.PAWN));
			}
		}
		
		// P0 cannot discard any piece (all RAINBOW ROOKs don't match WHITE KING)
		// Wait - RAINBOW can always be discarded! Let me fix this test
		// Actually, checking the rules: monopoly draw is "in lieu of any other action you would normally be forced to take"
		// So if they CAN discard, they should discard. Monopoly draw is only when forced to draw normally.
		
		// Let's change: P0 has all non-matching pieces
		p0.getPile().clear();
		for (int i = 0; i < 5; i++) {
			p0.getPile().add(new ChessPiece(Color.WHITE, PieceType.ROOK));
		}
		
		// Discard pile has RAINBOW KING (only type match would work)
		state.getDiscard().clear();
		state.getDiscard().add(new ChessPiece(Color.RAINBOW, PieceType.KING));
		
		// P0 cannot discard (no kings, no rainbow), so normally would draw from unowned
		// But with monopoly, can elect to draw WHITE pawn instead
		state.getUnowned().clear();
		state.getUnowned().add(new ChessPiece(Color.WHITE, PieceType.PAWN));
		state.getUnowned().add(new ChessPiece(Color.RAINBOW, PieceType.BISHOP));
		
		// Set P0 as current
		while (bl.getCurrentPlayer() != p0) {
			if (state.getUnowned().isEmpty()) {
				state.getUnowned().add(new ChessPiece(Color.WHITE, PieceType.PAWN));
			}
			bl.draw();
		}
		
		// Act & Assert: can use monopoly draw
		assertTrue(bl.canDrawMonopolyPawn(), "Player with monopoly and no discards should be able to monopoly draw");
		assertTrue(bl.drawMonopolyPawn(), "Monopoly draw should succeed");
	}
}
 
