package com.agent.orchestrator.exception;

public class UserException  extends Exception
{
    public Exception myexception()
    {
        return new UserException();
    }

}
