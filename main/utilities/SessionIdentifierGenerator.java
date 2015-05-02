package main.utilities;

import java.security.SecureRandom;
import java.math.BigInteger;

public final class SessionIdentifierGenerator
{

	private SecureRandom random = new SecureRandom();

	public String nextSessionId()
	{
		return new BigInteger(256, random).toString(32);
	}

	public static void main(String[] args) {
		SessionIdentifierGenerator generator = new SessionIdentifierGenerator();
		int i=5;
		while(i>1){
		System.out.println(generator.nextSessionId());
		i--;
		}
	}
}