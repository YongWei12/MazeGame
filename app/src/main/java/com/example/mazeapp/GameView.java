package com.example.mazeapp;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class GameView extends View {

    private enum Direction{
        UP, DOWN, LEFT, RIGHT
    }
    private Cell[][] cells;
    //thses 2 can be the blocks or obstacles and robots
    private Cell player, exit;
    private static final int COLS = 13, ROWS = 13;
    private static final float WALL_THICKNESS = 4;
    private float cellSize, hMargin, vMargin;
    private Random random;
    private Paint wallPaint, playerPaint, exitPaint;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        wallPaint = new Paint();
        wallPaint.setColor(Color.BLACK);
        wallPaint.setStrokeWidth(WALL_THICKNESS);

        playerPaint = new Paint();
        playerPaint.setColor(Color.RED);


        exitPaint = new Paint();
        exitPaint.setColor(Color.BLUE);



        random = new Random();
        createMaze();
    }


    //get the neighbour of current cell method
    private Cell getNeighbour(Cell cell){
        ArrayList<Cell> neighbours = new ArrayList<>();

        //left neighbour
        //only allow to check for neighbour if column of cell is greater than 0
        if(cell.col>0) {
            if (!cells[cell.col - 1][cell.row].visited) {
                neighbours.add(cells[cell.col - 1][cell.row]);
            }
        }

        //right neighbour
        //only allow to check for neighbour if column of cell does not exceed max column
        if(cell.col< COLS-1) {
            if (!cells[cell.col +1][cell.row].visited) {
                neighbours.add(cells[cell.col + 1][cell.row]);
            }
        }

        //top neighbour
        //only allow to check for neighbour if column of cell is greater than 0
        if(cell.row>0) {
            if (!cells[cell.col][cell.row -1].visited) {
                neighbours.add(cells[cell.col][cell.row -1]);
            }
        }

        //only allow to check for neighbour if column of cell is greater than 0
        if(cell.row < ROWS -1) {
            //left neighbour
            if (!cells[cell.col][cell.row +1 ].visited) {
                neighbours.add(cells[cell.col][cell.row +1]);
            }
        }

        // get a random neighbour
        if(neighbours.size()>0) {
            int index = random.nextInt(neighbours.size());
            return neighbours.get(index);
        }
        else{
            return null;
        }

    }

    private void removeWall(Cell current, Cell next){
        if(current.col == next.col && current.row== next.row+1){
            current.topWall =false;
            next.bottomWall = false;
        }

        if(current.col == next.col && current.row== next.row-1){
            current.bottomWall =false;
            next.topWall = false;
        }

        if(current.col == next.col+1 && current.row== next.row){
            current.leftWall =false;
            next.rightWall = false;
        }

        if(current.col == next.col-1 && current.row== next.row){
            current.rightWall =false;
            next.leftWall = false;
        }
    }


    private void createMaze(){
        cells = new Cell[COLS][ROWS];

        for (int x=0; x<COLS; x++){
            for (int y=0; y<ROWS; y++){
                cells[x][y] = new Cell(x, y);
            }
        }

        //create the player and exit cell
        player = cells[0][0];
        exit = cells[COLS -1][ROWS-1];

        //create a stack for the cells
        Stack<Cell> stack = new Stack<>();
        Cell current, next;

        //method that will remove the wall and generate the maze
        current = cells[0][0];
        do{
            next= getNeighbour(current);
            if(next != null){
                removeWall(current, next);
                stack.push(current);
                current = next;
                current.visited = true;
            }
            else {
                current= stack.pop();
            }
        }while (!stack.empty());

    }


   // called by system when gameview class is displayed
    @Override
    protected void onDraw(Canvas canvas) {
        //can use COLOR. method to define the required color
        canvas.drawColor(Color.GREEN);

        int width = getWidth();
        int height = getHeight();

        if (width/height  <  COLS/ROWS)
            cellSize = width/(COLS+1);
        else
            cellSize = height/(ROWS+1);

        hMargin = (width- COLS*cellSize)/2;
        vMargin = (height - ROWS*cellSize)/2;

        canvas.translate(hMargin, vMargin);

        for (int x=0; x<COLS; x++){
            for (int y=0; y<ROWS; y++){

                //if top wall is true
                if(cells[x][y].topWall){
                    canvas.drawLine(
                        x*cellSize,
                        y*cellSize,
                        (x+1)*cellSize,
                        y*cellSize,
                        wallPaint);
                }


                if(cells[x][y].leftWall){
                    canvas.drawLine(
                            x*cellSize,
                            y*cellSize,
                            x*cellSize,
                            (y+1)*cellSize,
                            wallPaint);
                }

                if(cells[x][y].bottomWall){
                    canvas.drawLine(
                            x*cellSize,
                            (y+1)*cellSize,
                            (x+1)*cellSize,
                            (y+1)*cellSize,
                            wallPaint);
                }


                if(cells[x][y].rightWall){
                    canvas.drawLine(
                            (x+1)*cellSize,
                            y*cellSize,
                            (x+1)*cellSize,
                            (y+1)*cellSize,
                            wallPaint);
                }
            }
        }

        canvas.drawRect(
                player.col*cellSize,
                player.row*cellSize,
                (player.col+1)*cellSize,
                (player.row+1)*cellSize,
                playerPaint
        );


        canvas.drawRect(
                exit.col*cellSize,
                exit.row*cellSize,
                (exit.col+1)*cellSize,
                (exit.row+1)*cellSize,
                exitPaint
        );
    }


    private void movePlayer (Direction direction){
        switch(direction){
            case UP:
                if(!player.topWall)
                     player = cells[player.col][player.row-1];
                break;

            case DOWN:
                if(!player.bottomWall)
                    player = cells[player.col][player.row+1];
                break;

            case LEFT:
                if(!player.leftWall)
                    player = cells[player.col-1][player.row];
                break;

            case RIGHT:
                if(!player.rightWall)
                    player = cells[player.col+1][player.row];
                break;
        }
        //calls onDraw method as soon as possible
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_DOWN)
            return  true;
        if(event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getX();
            float y = event.getY();

            float playerCenterX = (player.col+.5f) * cellSize;
            float playerCenterY = (player.row + .5f)* cellSize;

            //difference in x and y direction
            float dx = x- playerCenterX;
            float dy = y - playerCenterY;


            //abs distance for movement
            float absDx = Math.abs(dx);
            float absDy= Math.abs(dy);

            if(absDx>cellSize || absDy>cellSize){
                if(absDx > absDy){
                    //move in x direction
                    if(dx>0){
                        //move to the right
                        movePlayer(Direction.RIGHT);
                    }
                    else{
                        //move to the left
                        movePlayer(Direction.LEFT);
                    }

                }

                else{
                    //move in y direction
                    if(dy>0){
                        //move down
                        movePlayer(Direction.DOWN);
                    }
                    else{
                        //move up
                        movePlayer(Direction.UP);
                    }
                }
            }
            return true;
        }

        return super.onTouchEvent(event);
    }

    // class for the individual cells in the map
private class Cell{
        boolean
                topWall =true,
                leftWall = true,
                rightWall = true,
                bottomWall = true,
                visited = false;

        int col, row;

        public Cell(int col, int row){
            this.col = col;
            this.row = row;
        }
}
}
