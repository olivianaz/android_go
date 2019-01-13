package net.oliviana.playgo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.v7.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

    public class StoneGroup {
        int player;
        Set<String> points;
        Set<String> liberties;
        Set<String> adjOpponentStones;

        // create a stonegroup for one point
        StoneGroup(int row, int col, int player, int[][] board){
            String pointString = row + "," + col;
            String[] corners = {"0,0","0,8","8,0","8,8"};
            List<String> cornerList = Arrays.asList(corners);

            this.player = player;
            this.points = new HashSet<String>();
            this.points.add(row + "," + col);

            this.liberties = new HashSet<String>();
            this.adjOpponentStones = new HashSet<String>();
            try {
                // top
                if (board[row - 1][col] == 0) {
                    this.liberties.add(row - 1 + "," + col);
                }
                else if (board[row-1][col] != player){
                    this.adjOpponentStones.add(row - 1 + "," + col);
                }
            }
            catch (ArrayIndexOutOfBoundsException e){}
            try {
                // left
                if (board[row][col - 1] == 0) {
                    this.liberties.add(row + "," + (col - 1));
                }
                else if (board[row][col-1] != player){
                    this.adjOpponentStones.add(row + "," + (col-1));
                }

            }
            catch (ArrayIndexOutOfBoundsException e){}
            try {
                // right
                if (board[row][col + 1] == 0) {
                    this.liberties.add(row + "," + (col + 1));
                }
                else if (board[row][col+1] != player){
                    this.adjOpponentStones.add(row + "," + (col+1));
                }

            }
            catch (ArrayIndexOutOfBoundsException e){}
            try {
            // bottom
                if (board[row+1][col] == 0){
                    this.liberties.add((row+1) + "," + (col));
                }
                else if (board[row+1][col] != player){
                    this.adjOpponentStones.add(row + 1 + "," + col);
                }

            }
            catch (ArrayIndexOutOfBoundsException e){
                // do nothing
                Log.i("exception", e.getClass().toString());
            }

            Log.i("CREATE", pointString + ", adjacent: " + this.adjOpponentStones + ", liberties: " + this.liberties);

            // update opponent's adjacencies
            for (String opponentStone: this.adjOpponentStones){
                StoneGroup opponentGroup = stoneToGroup.get(opponentStone);
                opponentGroup.liberties.remove(pointString);
                opponentGroup.adjOpponentStones.add(pointString);
            }
        }
    }

    public StoneGroup combineStoneGroups(List<StoneGroup> groups){
        StoneGroup combinedGroup = groups.get(0);   // use the first stone group as the new combined group

        for (StoneGroup sg: groups){
            combinedGroup.points.addAll(sg.points);
            combinedGroup.liberties.addAll(sg.liberties);
        }
        combinedGroup.liberties.removeAll(combinedGroup.points);
        return combinedGroup;
    }

    public void logBoardStatus(){
        String statusString = "-----------\n";
        for (int i = 0; i < 9; i++){
            statusString = statusString + "\n";
            for (int j = 0; j < 9; j++) {
                statusString = statusString + " " + Integer.toString(points[i][j]);
            }
        }
        Log.i("Board", statusString);
    }

    public void capture(StoneGroup sg){
        // remove the stone group from the board
        for (String stone: sg.points){
            int r;
            int c;
            String[] coordinates = stone.split(",");
            r = Integer.parseInt(coordinates[0]);
            c = Integer.parseInt(coordinates[1]);
            points[r][c] = 0;
            ImageView pointImageView = pointToImageView.get(stone);
            pointImageView.setImageDrawable(null);
            stoneToGroup.remove(stone);
        }
        Log.i("REMOVE", "Adjacent opponents: " + sg.adjOpponentStones);
        // and update adjacent groups' liberties
        for (String opponentStone: sg.adjOpponentStones){
            // opponent will get the following added to its liberties:
            // intersection of sg.points and opponent's adjacent stones

            StoneGroup opponentGroup = stoneToGroup.get(opponentStone);
            Log.i("Update opponent", opponentStone + ", points size=" + sg.points.size() + ",liberties=" + opponentGroup.liberties);

            Set<String> newFreePoints = new HashSet<String>(sg.points);
            newFreePoints.retainAll(opponentGroup.adjOpponentStones);
            opponentGroup.liberties.addAll(newFreePoints);

            opponentGroup.adjOpponentStones.removeAll(newFreePoints);
        }
    }

    int player; // specifies whose turn it is, 1 = black, 2 = white

    int [][] points = new int[9][9]; // keeps track of what occupies each point (1=black, 2=white, 0=empty)
    Map<String, ImageView> pointToImageView = new HashMap<String, ImageView>();
    Map<String, StoneGroup> stoneToGroup = new HashMap<String, StoneGroup>();    // maps each point to a group of stones

    // make the move to row r, column c, and update the board status
    public int makeMove(int r, int c, int lastPlayer){
        int captures = 0;

        List<String> opponentStones = new ArrayList<String>();
        Set<StoneGroup> opponentStoneGroups = new HashSet<StoneGroup>();
        List<String> friendStones = new ArrayList<String>();
        Set<StoneGroup> friendStoneGroups = new HashSet<StoneGroup>();
        try {
            // top
            if (points[r - 1][c] > 0) {
                if (points[r - 1][c] != lastPlayer) {
                    opponentStones.add((r - 1) + "," + c);
                } else {
                    friendStones.add((r - 1) + "," + c);
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e){
            // do nothing
        }
        try {
            // left
            if (points[r][c - 1] > 0) {
                if (points[r][c - 1] != lastPlayer) {
                    opponentStones.add(r + "," + (c - 1));
                } else {
                    friendStones.add(r + "," + (c - 1));
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e){
            // do nothing
        }
        try {
            // right
            if (points[r][c + 1] > 0) {
                if (points[r][c + 1] != lastPlayer) {
                    opponentStones.add(r + "," + (c + 1));
                } else {
                    friendStones.add(r + "," + (c + 1));
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e){
            // do nothing
        }
        try {
            // bottom
            if (points[r + 1][c] > 0) {
                if (points[r + 1][c] != lastPlayer) {
                    opponentStones.add((r + 1) + "," + c);
                } else {
                    friendStones.add((r + 1) + "," + c);
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e){
            // do nothing
        }

        // update liberties of opponents' stones
        for (String stone: opponentStones){
            StoneGroup sg = stoneToGroup.get(stone);
            sg.liberties.remove(r + "," + c);
            Log.i("liberties", stone + "/" + sg.liberties.size());

            if (sg.liberties.size() == 0){
                Log.i("info", "Group at " + stone + " should be captured.");
                capture(sg);
            }
        }

        // make captures here and update opponentStones

        Log.i("Before create", "Board status");
        logBoardStatus();

        // create a stone group for the new move and combine it with adjacent friend groups
        points[r][c] = lastPlayer;
        StoneGroup newSg = new StoneGroup(r, c, lastPlayer, points);
        stoneToGroup.put(r + "," + c, newSg);

        // has adjacent 'friend' stones, so combine the stone to this stone group
        // and update the liberty points
        for (String stone: friendStones){
            friendStoneGroups.add(stoneToGroup.get(stone));
        }

        List<StoneGroup> friendStoneGroupList = new ArrayList<StoneGroup>(friendStoneGroups);
        friendStoneGroupList.add(newSg);
        StoneGroup combinedGrp = combineStoneGroups(friendStoneGroupList);
        for (String stone: combinedGrp.points){
            stoneToGroup.put(stone, combinedGrp);
        }
        Log.i("group info", "Liberties: " + combinedGrp.liberties.size() + " size: " + combinedGrp.points.size());

        return captures;
    }

    public void dropIn(View view){
        // set the resource to the stone's image
        // and animate the stone being put on the board
        ImageView stone = (ImageView) view;

        // check that the clicked point is not yet occupied
        // before placing the stone there
        String[] positionString = ((String) view.getTag()).split(",");

        int row = Integer.parseInt(positionString[0]);
        int col = Integer.parseInt(positionString[1]);
        int capturedStones = 0;

        if (points[row][col] > 0){
            Toast.makeText(this, "Please click on an empty point.", Toast.LENGTH_SHORT).show();
        }
        else {

            if (player == 1) {
                stone.setImageResource(R.drawable.go_stone_black);
                capturedStones = makeMove(row, col, player);
                player = 2;
            }
            else {
                stone.setImageResource(R.drawable.go_stone_white);
                capturedStones = makeMove(row, col, player);
                player = 1;
            }
            stone.animate().setDuration(300);
            logBoardStatus();
        }
    }

    public void setupBoard(){
        // initialize the game board
        player = 1; // by default, black goes first

        GridLayout gameBoardGridLayout = (GridLayout) findViewById(R.id.gameBoardGridLayout);
        for(int i=0; i<gameBoardGridLayout.getChildCount(); i++) {
            ImageView point = (ImageView) gameBoardGridLayout.getChildAt(i);
            pointToImageView.put((String) point.getTag(), point);   // store the mapping of coordinates to ImageView, so we could easily access
                                                                    // the ImageView
            point.setImageDrawable(null);
        }

        for (int[] row: points){
            for (int i = 0; i < 9; i++){
                row[i] = 0;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupBoard();
    }
}
