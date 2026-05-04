package br.com.threadstech.stockfy.users.application.port;

public interface ResetCodeHasher {
  String hash(String rawCode);
}
