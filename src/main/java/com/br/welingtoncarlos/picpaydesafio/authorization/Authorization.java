package com.br.welingtoncarlos.picpaydesafio.authorization;

public record Authorization(
    String message
) {
    public boolean isAuthorized() {
        return message.equals("Authorizado");
    }
    
}
