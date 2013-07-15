/*
 * Copyright (c) 2010, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  * Neither the name of the University of California, Berkeley
 * nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Author: Marco Guazzone (marco.guazzone@gmail.com), 2013.
 */

package radlab.rain.workload.rubis.util;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import org.json.JSONObject;
import org.json.JSONException;
import radlab.rain.ScenarioTrack;
import radlab.rain.workload.rubis.model.RubisComment;
import radlab.rain.workload.rubis.model.RubisItem;
import radlab.rain.workload.rubis.model.RubisUser;
import radlab.rain.workload.rubis.RubisConfiguration;
import radlab.rain.workload.rubis.RubisUtility;


/**
 * Handle the initialization of the RUBiS database.
 *
 * @author Marco Guazzone (marco.guazzone@gmail.com)
 */
final class InitDb
{
	private static final String SQL_DELETE_USERS = "DELETE FROM users";
	private static final String SQL_INSERT_USER = "INSERT INTO users (id,firstname,lastname,nickname,password,email,rating,balance,creation_date,region) VALUES (?,?,?,?,?,?,?,?,?,?)";
	private static final String SQL_DELETE_ITEMS = "DELETE FROM items";
	private static final String SQL_INSERT_ITEM = "INSERT INTO items (id,name,description,initial_price,quantity,reserve_price,buy_now,nb_of_bids,max_bid,start_date,end_date,seller,category) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static final String SQL_DELETE_BIDS = "DELETE FROM bids";
	private static final String SQL_INSERT_BID = "INSERT INTO bids (id,user_id,item_id,qty,bid,max_bid,date) VALUES (NULL,?,?,?,?,?,?)";
	private static final String SQL_DELETE_COMMENTS = "DELETE FROM bids";
	private static final String SQL_INSERT_COMMENT = "INSERT INTO bids (id,user_id,item_id,qty,bid,max_bid,date) VALUES (NULL,?,?,?,?,?,?)";


	private RubisConfiguration _conf;
	private RubisUtility _util;
	private Connection _dbConn;
	private PrintWriter _pwr;
	private boolean _testFlag;


	public InitDb(RubisConfiguration conf, Connection dbConn)
	{
		this._conf = conf;
		this._util = new RubisUtility(new Random(conf.getRngSeed()), conf);
		this._dbConn = dbConn;
		this._testFlag = false;
		this._pwr = null;
	}

	public void setTestOnlyFlag(boolean value)
	{
		this._testFlag = value;
	}

	public boolean testOnly()
	{
		return this._testFlag;
	}


	public void setSqlWriter(PrintWriter pwr)
	{
		this._pwr = pwr;
	}

	public PrintWriter getSqlWriter()
	{
		return this._pwr;
	}

	public void initialize() throws Exception
	{
		this.initializeUsers();
		this.initializeItems();
	}

	private void initializeUsers() throws Exception
	{
		Statement stmt = null;
		PreparedStatement prepStmt = null;

		try
		{
			if (!this._testFlag)
			{
				this._dbConn.setAutoCommit(false);

			}

			int minId = 1;

			// Delete all existing users
			if (!this._testFlag)
			{
				stmt = this._dbConn.createStatement();
				int affectedRows = stmt.executeUpdate(SQL_DELETE_USERS, Statement.RETURN_GENERATED_KEYS);
				if (affectedRows > 0)
				{
					ResultSet rs = stmt.getGeneratedKeys();
					if (rs.next())
					{
						minId = rs.getInt(1)+1;
					}
					rs.close();
				}
			}
			if (this._pwr != null)
			{
				this._pwr.println(this._dbConn.nativeSQL(SQL_DELETE_USERS));
			}

			// Generate users
			final int maxId = this._conf.getNumOfPreloadedUsers()-minId;
			prepStmt = this._dbConn.prepareStatement(SQL_INSERT_USER, Statement.RETURN_GENERATED_KEYS);
			for (int id = minId; id <= maxId; ++id)
			{
				RubisUser user = this._util.getUser(id);

				prepStmt.clearParameters();
				prepStmt.setInt(1, user.id);
				prepStmt.setString(2, user.firstname);
				prepStmt.setString(3, user.lastname);
				prepStmt.setString(4, user.nickname);
				prepStmt.setString(5, user.password);
				prepStmt.setString(6, user.email);
				prepStmt.setInt(7, user.rating);
				prepStmt.setDouble(8, user.balance);
				prepStmt.setDate(9, new Date(user.creationDate.getTime()));
				prepStmt.setInt(10, user.region);

				if (!this._testFlag)
				{
					int affectedRows = prepStmt.executeUpdate();

					// Experimental: use batches instead of execute one query at a time
					//prepStmt.addBatch();
					//if ((id % 1000) == 0)
					//{
					//	prepStmt.executeBatch();
					//}

					if (affectedRows == 0)
					{
						throw new SQLException("During user insertion: No rows affected");
					}

					ResultSet rs = prepStmt.getGeneratedKeys();
					if (rs.last())
					{
						int genId = rs.getInt(1);

						if (id != genId)
						{
							System.err.println("[WARNING] Expected user ID (" + id + ") is different from the one that has been generated (" + genId + ")");
						}
					}
					rs.close();
				}
				if (this._pwr != null)
				{
					this._pwr.println(prepStmt);
				}
			}

			if (!this._testFlag)
			{
				this._dbConn.commit();
			}
		}
		catch (SQLException se)
		{
			if (!this._testFlag)
			{
				this._dbConn.rollback();
			}

			throw se;
		}
		finally
		{
			if (!this._testFlag)
			{
				if (stmt != null)
				{
					stmt.close();
				}
				if (prepStmt != null)
				{
					prepStmt.close();
				}
				this._dbConn.setAutoCommit(true);
			}
		}
	}

	private void initializeItems() throws Exception
	{
		Statement stmt = null;
		PreparedStatement itemStmt = null;
		PreparedStatement bidStmt = null;
		PreparedStatement comStmt = null;

		try
		{
			if (!this._testFlag)
			{
				this._dbConn.setAutoCommit(false);

			}

			int minId = 1;

			// Delete all existing items
			if (!this._testFlag)
			{
				stmt = this._dbConn.createStatement();
				int affectedRows = stmt.executeUpdate(SQL_DELETE_ITEMS, Statement.RETURN_GENERATED_KEYS);
				if (affectedRows > 0)
				{
					ResultSet rs = stmt.getGeneratedKeys();
					if (rs.next())
					{
						minId = rs.getInt(1)+1;
					}
					rs.close();
				}
			}
			if (this._pwr != null)
			{
				this._pwr.println(this._dbConn.nativeSQL(SQL_DELETE_ITEMS));
			}

			// Generate item
			final int maxId = this._conf.getTotalActiveItems()+this._conf.getNumOfOldItems()-minId;
			final int maxOldItemId = this._conf.getNumOfOldItems()-minId;
			int count = 0;
			itemStmt = this._dbConn.prepareStatement(SQL_INSERT_ITEM, Statement.RETURN_GENERATED_KEYS);
			bidStmt = this._dbConn.prepareStatement(SQL_INSERT_BID, Statement.RETURN_GENERATED_KEYS);
			comStmt = this._dbConn.prepareStatement(SQL_INSERT_COMMENT, Statement.RETURN_GENERATED_KEYS);
			for (int id = minId; id <= maxId; ++id)
			{
				++count;

				RubisUser seller = this._util.generateUser();
				RubisItem item = this._util.getItem(id, seller.id);

				if (id <= maxOldItemId)
				{
					// Generate an old item whereby auction date is over

					// Add a negative duration so that the auctio will be over
					int duration = this._util.getDaysBetween(item.startDate, item.endDate);
					item.endDate = this._util.addDays(item.startDate, -duration);

					if (count < (this._conf.getPercentageOfItemsReserve()*this._conf.getNumOfOldItems()/100.0))
					{
						item.reservePrice = this._util.getRandomGenerator().nextInt(Math.round(this._conf.getMaxItemBaseReservePrice())) + item.initialPrice;
					}
					else
					{
						item.reservePrice = 0;
					}
					if (count < (this._conf.getPercentageOfItemsBuyNow()*this._conf.getNumOfOldItems()/100.0))
					{
						item.buyNow = this._util.getRandomGenerator().nextInt(Math.round(this._conf.getMaxItemBaseBuyNowPrice())) + item.initialPrice + item.reservePrice;
					}
					else
					{
						item.buyNow = 0;
					}
					if (count < (this._conf.getPercentageOfUniqueItems()*this._conf.getNumOfOldItems()/100.0))
					{
						item.quantity = 1;
					}
					else
					{
						item.quantity = this._util.getRandomGenerator().nextInt(Math.round(this._conf.getMaxItemQuantity())) + 1;
					}
				}

				itemStmt.clearParameters();
				itemStmt.setInt(1, item.id);
				itemStmt.setString(2, item.name);
				itemStmt.setString(3, item.description);
				itemStmt.setFloat(4, item.initialPrice);
				itemStmt.setInt(5, item.quantity);
				itemStmt.setFloat(6, item.reservePrice);
				itemStmt.setFloat(7, item.buyNow);
				itemStmt.setInt(8, item.nbOfBids);
				itemStmt.setFloat(9, item.maxBid);
				itemStmt.setDate(10, new Date(item.startDate.getTime()));
				itemStmt.setDate(11, new Date(item.endDate.getTime()));
				itemStmt.setInt(12, item.seller);
				itemStmt.setInt(13, item.category);

				if (!this._testFlag)
				{
					int affectedRows = itemStmt.executeUpdate();

					// Experimental: use batches instead of execute one query at a time
					//itemStmt.addBatch();
					//if ((id % 1000) == 0)
					//{
					//	itemStmt.executeBatch();
					//}

					if (affectedRows == 0)
					{
						throw new SQLException("During item insertion: No rows affected");
					}

					ResultSet rs = itemStmt.getGeneratedKeys();
					if (rs.last())
					{
						int genId = rs.getInt(1);

						if (id != genId)
						{
							System.err.println("[WARNING] Expected item ID (" + id + ") is different from the one that has been generated (" + genId + ")");
						}
					}
					rs.close();
				}
				if (this._pwr != null)
				{
					this._pwr.println(itemStmt);
				}


				this.initializeBids(item, bidStmt);
				this.initializeComments(item, bidStmt);
			}

			if (!this._testFlag)
			{
				this._dbConn.commit();
			}
		}
		catch (SQLException se)
		{
			if (!this._testFlag)
			{
				this._dbConn.rollback();
			}

			throw se;
		}
		finally
		{
			if (!this._testFlag)
			{
				if (stmt != null)
				{
					stmt.close();
				}
				if (itemStmt != null)
				{
					itemStmt.close();
				}
				if (bidStmt != null)
				{
					bidStmt.close();
				}
				if (comStmt != null)
				{
					comStmt.close();
				}

				this._dbConn.setAutoCommit(true);
			}
		}
	}

	private void initializeBids(RubisItem item, PreparedStatement bidStmt) throws SQLException
	{
		Statement stmt = null;

		try
		{
			// Delete all existing items
			if (!this._testFlag)
			{
				stmt = this._dbConn.createStatement();
				stmt.executeUpdate(SQL_DELETE_BIDS);
			}
			if (this._pwr != null)
			{
				this._pwr.println(this._dbConn.nativeSQL(SQL_DELETE_BIDS));
			}
			// Generate bids
			final int nbids = this._conf.getMaxBidsPerItem();
			float minBid = item.initialPrice;
			for (int j = 0; j < nbids; ++j)
			{
				final int userId = this._util.generateUser().id;
				final int addBid = this._util.getRandomGenerator().nextInt(Math.round(this._conf.getMaxItemBaseBidPrice()))+1;
				final int qty = this._util.getRandomGenerator().nextInt(item.quantity)+1;
				final float bid = minBid + addBid;
				final float maxBid = minBid + addBid * 2;
				final Date dtNow = new Date(System.currentTimeMillis());

				bidStmt.clearParameters();
				bidStmt.setInt(1, userId);
				bidStmt.setInt(2, item.id);
				bidStmt.setInt(3, qty);
				bidStmt.setFloat(4, bid);
				bidStmt.setFloat(5, maxBid);
				bidStmt.setDate(6, dtNow);

				if (!this._testFlag)
				{
					int affectedRows = bidStmt.executeUpdate();

					if (affectedRows == 0)
					{
						throw new SQLException("During bid insertion: No rows affected");
					}
				}
				if (this._pwr != null)
				{
					this._pwr.println(bidStmt);
				}

				minBid += addBid;
			}
		}
		finally
		{
			if (stmt != null)
			{
				stmt.close();
			}
		}
	}

	private void initializeComments(RubisItem item, PreparedStatement comStmt) throws SQLException
	{
		Statement stmt = null;

		try
		{
			// Delete all existing items
			if (!this._testFlag)
			{
				stmt = this._dbConn.createStatement();
				stmt.executeUpdate(SQL_DELETE_COMMENTS);
			}
			if (this._pwr != null)
			{
				this._pwr.println(this._dbConn.nativeSQL(SQL_DELETE_COMMENTS));
			}

			RubisComment comment = this._util.generateComment(this._util.generateUser().id,
															  item.seller,
															  item.id);

			comStmt.clearParameters();
			comStmt.setInt(1, comment.fromUserId);
			comStmt.setInt(2, comment.toUserId);
			comStmt.setInt(3, comment.itemId);
			comStmt.setInt(4, comment.rating);
			comStmt.setDate(5, new Date(comment.date.getTime()));

			if (!this._testFlag)
			{
				int affectedRows = comStmt.executeUpdate();

				if (affectedRows == 0)
				{
					throw new SQLException("During comment insertion: No rows affected");
				}
			}
			if (this._pwr != null)
			{
				this._pwr.println(comStmt);
			}
		}
		finally
		{
			if (stmt != null)
			{
				stmt.close();
			}
		}
	}
}

public final class InitDbDriver
{
	private static String DEFAULT_OPT_CONFIG_FILE = "config/profiles.config.rubis.json";
	private static String DEFAULT_OPT_DB_URL = "jdbc:mysql://localhost/rubis";
	private static String DEFAULT_OPT_DB_USER = "";
	private static String DEFAULT_OPT_DB_PASSWORD = "";
	private static boolean DEFAULT_OPT_TEST = false;
	private static boolean DEFAULT_OPT_DUMP = false;
	private static String DEFAULT_OPT_DUMP_FILE = "";


	public static void main(String[] args)
	{
		String optConfigFile = DEFAULT_OPT_CONFIG_FILE;
		String optDbUrl = DEFAULT_OPT_DB_URL;
		String optDbUser = DEFAULT_OPT_DB_USER;
		String optDbPassword = DEFAULT_OPT_DB_PASSWORD;
		boolean optTest = DEFAULT_OPT_TEST;
		boolean optDump = DEFAULT_OPT_DUMP;
		String optDumpFile = DEFAULT_OPT_DUMP_FILE;

		// Parse command line args
		for (int i = 0; i < args.length; ++i)
		{
			if (args[i].equals("-conf"))
			{
				optConfigFile = args[i+1];
				++i;
			}
			else if (args[i].equals("-dburl"))
			{
				optDbUrl = args[i+1];
				++i;
			}
			else if (args[i].equals("-dbusr"))
			{
				optDbUser = args[i+1];
				++i;
			}
			else if (args[i].equals("-dbpwd"))
			{
				optDbPassword = args[i+1];
				++i;
			}
			else if (args[i].equals("-test"))
			{
				optTest = true;
				++i;
			}
			else if (args[i].equals("-dump"))
			{
				optDump = true;
				++i;
			}
			else if (args[i].equals("-dumpfile"))
			{
				optDump = true;
				optDumpFile = args[i+1];
				++i;
			}
			else if (args[i].equals("-help"))
			{
				usage();
				System.exit(0);
			}
		}

		StringBuffer sb = new StringBuffer();
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(optConfigFile));
			while (br.ready())
			{
				sb.append(br.readLine());
			}
		}
		catch (IOException ioe)
		{
			System.err.println("[ERROR] Cannot read configuration file '" + optConfigFile + ": " + ioe);
			System.exit(-1);
		}

		RubisConfiguration conf = null;
		try
		{
			JSONObject json = new JSONObject(sb.toString());
			if (json.has(ScenarioTrack.CFG_GENERATOR_PARAMS_KEY))
			{
				conf = new RubisConfiguration(json.getJSONObject(ScenarioTrack.CFG_GENERATOR_PARAMS_KEY));
			}
			else
			{
				// Try to use default values

				System.err.println("[WARNING] No generator parameters has been found. Try to use default values.");

				conf = new RubisConfiguration();
			}
		}
		catch (JSONException je)
		{
			System.err.println("[ERROR] Cannot parse configuration file '" + optConfigFile + ": " + je);
			System.exit(-1);
		}

		Connection dbConn = null;
		try
		{
			if (optDbUser.isEmpty())
			{
				dbConn = DriverManager.getConnection(optDbUrl);
			}
			else
			{
				dbConn = DriverManager.getConnection(optDbUrl, optDbUser, optDbPassword);
			}
		}
		catch (SQLException se)
		{
			System.err.println("[ERROR] Cannot open a connection to database '" + optDbUrl + ": " + se);
			System.exit(-1);
		}

		InitDb initDb = new InitDb(conf, dbConn);

		initDb.setTestOnlyFlag(optTest);
		PrintWriter dumpWr = null;
		if (optDump)
		{
			if (optDumpFile.isEmpty())
			{
				dumpWr = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
			}
			else
			{
				try
				{
					dumpWr = new PrintWriter(new BufferedWriter(new FileWriter(optDumpFile)));
				}
				catch (IOException ioe)
				{
					System.err.println("[ERROR] Cannot open dump file '" + optDumpFile + "': " + ioe);
					System.exit(-1);
				}
			}
		}

		try
		{
			initDb.initialize();
		}
		catch (Exception e)
		{
			System.err.println("[ERROR] Cannot initialize database: " + e);
			e.printStackTrace();
			System.exit(-1);
		}
		finally
		{
			if (dumpWr != null)
			{
				dumpWr.close();
			}
		}
	}

	private static void usage()
	{
		System.err.println("Usage: " + InitDbDriver.class.getCanonicalName() + " [options]");
		System.err.println("Options:");
		System.err.println(" -conf <file-path>: Path to the profiles configuration file.");
		System.err.println("  [Default='" + DEFAULT_OPT_CONFIG_FILE + "']");
		System.err.println(" -dburl <url>: A database url of the form jdbc:subprotocol:subname.");
		System.err.println("  [Default='" + DEFAULT_OPT_DB_URL + "']");
		System.err.println(" -dbusr <username>: The database user on whose behalf the connection is being made.");
		System.err.println("  [Default='" + DEFAULT_OPT_DB_USER + "']");
		System.err.println(" -dbpwd <password>: The user's password.");
		System.err.println("  [Default='" + DEFAULT_OPT_DB_PASSWORD + "']");
		System.err.println(" -test: Does not perform any operation inside the database.");
		System.err.println("  [Default='" + DEFAULT_OPT_TEST + "']");
		System.err.println(" -dump: Dump the generated SQL on the <dumpfile> (if specified) or on standard output.");
		System.err.println("  [Default='" + DEFAULT_OPT_DUMP + "']");
		System.err.println(" -dumpfile <filename>: Dump the SQL on the specified file.");
		System.err.println("  This option implies the '-dump' option.");
		System.err.println("  [Default='" + DEFAULT_OPT_DUMP_FILE + "']");
	}
}
