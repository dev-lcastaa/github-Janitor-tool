package xyz.aqlabs.janitor_tool.utils;

import java.util.Map;

public class Constants {

    private Constants(){}

    public static final String GITHUB_API_ENDPOINT = "https://api.github.com";
    public static final String GITHUB_API_ORGANIZATION = "/organizations/%s";
    public static final String GITHUB_API_REPOS = GITHUB_API_ENDPOINT + GITHUB_API_ORGANIZATION + "/repos";
    public static final String GITHUB_API_COMMITS = GITHUB_API_ENDPOINT + "/repos/%s/%s" + "/commits?sha=%s" ;
    public static final String GITHUB_API_DELETE_BRANCH_API = GITHUB_API_ENDPOINT + "/repos/%s/%s/git/refs/heads/%s";
    public static final String BRANCH_APPENDAGE = "{/branch}";


}
