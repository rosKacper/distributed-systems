package com.java.main;


import com.java.Team;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class MainTeam {

    public static void main(String[] args) throws IOException, TimeoutException {

        System.out.println("Podaj nazwe ekipy: ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        Team team = new Team(br.readLine());
        team.launch();
    }

}
