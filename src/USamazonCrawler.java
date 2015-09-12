import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//import javax.lang.model.util.Elements;
import javax.swing.DefaultListModel;

public class USamazonCrawler extends Thread {
	private int signal = 0;
	private DefaultListModel<String> listModel;
	private InfoCollect infoCollect;

	public USamazonCrawler(InfoCollect infoCollect,DefaultListModel<String> listModel) {
		// TODO 自動生成されたコンストラクター・スタブ
		this.listModel = listModel;
		this.infoCollect = infoCollect;
		

	}

	@Override
	public void run() {
		//
		while (true) {
			//System.out.println("bbb");
			System.out.println(signal);
			if (signal == 1) {
				System.out.println("aaa");
				if (popfromQueue() == false) {
					processPause();
				}

			}

		}

	}

	private boolean popfromQueue() {

		long id;
		int ic_class;
		String idString;
		String sql;
		boolean res = true;

		try {
			// JDBCドライバの登録
			String driver = "org.postgresql.Driver";
			// データベースの指定
			String server = "localhost"; // PostgreSQL サーバ ( IP または ホスト名 )
			String dbname = "db_usamazon"; // データベース名
			String url = "jdbc:postgresql://" + server + "/" + dbname;
			String user = "postgres"; // データベース作成ユーザ名
			String password = "wth050527"; // データベース作成ユーザパスワード
			Class.forName(driver);
			// データベースとの接続
			Connection con = DriverManager.getConnection(url, user, password);
			// テーブル照会実行
			Statement stmt = con.createStatement();
			sql = "SELECT COUNT(*) FROM worklist_tbl";
			// sql = "INSERT INTO tbl_test VALUES (4,'A.Jolly','388928839')";

			ResultSet rs = stmt.executeQuery(sql);
			// テーブル照会結果を出力
			rs.next();
			System.out.println(rs.getInt(1));
			if (rs.getInt(1) == 0) {
				res = false;
				rs.close();
				stmt.close();
				con.close();
			} else {

				// テーブル照会実行
				stmt = con.createStatement();
				sql = "SELECT * FROM worklist_tbl";
				rs = stmt.executeQuery(sql);

				rs.next();
				id = rs.getLong("id");
				ic_class = rs.getInt("class");
				idString = rs.getString("targetid");
				System.out.println("class：" + ic_class);
				System.out.println("targetid：" + idString);

				// 読みだしたレコードを削除
				stmt = con.createStatement();
				sql = "DELETE FROM worklist_tbl WHERE id = " + id + ";";
				rs = stmt.executeQuery(sql);

				// データベースのクローズ
				rs.close();
				stmt.close();
				con.close();

				if (ic_class == 0) {
					try {
						saveItem(idString);
					} catch (IOException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}
				} else if (ic_class == 1) {
					saveCustom(idString);
				}
			}

		} catch (SQLException e) {
			System.err.println("SQL failed.");
			e.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}

		return res;

	}

	private void saveItem(String idString) throws IOException {
		Document document1 = Jsoup.connect(
				"http://www.amazon.com/dp/" + idString).get();
		Document tmpReviewPage = Jsoup
				.connect(
						"http://www.amazon.com/product-reviews/"
								+ idString
								+ "/ref=cm_cr_pr_viewopt_srt?ie=UTF8&showViewpoints=1&sortBy=recent&reviewerType=all_reviews&formatType=all_formats&filterByStar=all_stars&pageNumber=1")
				.get();
		Element revcount = tmpReviewPage.getElementsByClass("totalReviewCount")
				.get(0);
		int totalReviewCount = Integer.parseInt(revcount.text().replaceAll(",",
				""));
		ArrayList<Document> revpagelist = new ArrayList<Document>();
		ArrayList<String> customers = new ArrayList<String>();
		for (int i = 0; i < Math.ceil(totalReviewCount * 0.1); i++) {
			revpagelist
					.add(Jsoup
							.connect(
									"http://www.amazon.com/product-reviews/"
											+ idString
											+ "/ref=cm_cr_pr_viewopt_srt?ie=UTF8&showViewpoints=1&sortBy=recent&reviewerType=all_reviews&formatType=all_formats&filterByStar=all_stars&pageNumber="
											+ (i + 1)).get());
		}

		try {
			// JDBCドライバの登録
			String driver = "org.postgresql.Driver";
			// データベースの指定
			String server = "localhost"; // PostgreSQL サーバ ( IP または ホスト名 )
			String dbname = "db_usamazon"; // データベース名
			String url = "jdbc:postgresql://" + server + "/" + dbname;
			String user = "postgres"; // データベース作成ユーザ名
			String password = "wth050527"; // データベース作成ユーザパスワード
			Class.forName(driver);
			// データベースとの接続
			Connection con = DriverManager.getConnection(url, user, password);
			// テーブル照会実行
			Statement stmt = con.createStatement();
			String sql;
			ResultSet rs;

			for (Document d : revpagelist) {

				Elements tmpElements = d.getElementsByClass("review");
				for (Element element : tmpElements) {
					int rating = Character.getNumericValue(element
							.getElementsByClass("a-icon-alt").get(0).text()
							.charAt(0)); // 星の数
					String customer = element.getElementsByClass("author")
							.get(0).attr("href").split("/")[4]; // 投稿者ID
					String reviewid = element.attr("id"); // レビューID
					String reviewdate = element
							.getElementsByClass("review-date").get(0).text()
							.substring(2); // 投稿日
					
					

					String vote_help_senten = element
							.getElementsByClass("helpful-votes-count").get(0)
							.text();

					Pattern p = Pattern.compile("[0-9]+");
					Matcher m = p.matcher(vote_help_senten);

					m.find();
					int helpful = Integer.parseInt(m.group()); // 役立ち人数
					m.find();
					int votes = Integer.parseInt(m.group()); // 投票総数

					// テーブル書き込み実行
					stmt = con.createStatement();
					sql = "INSERT INTO worklist_tbl (class,targetid) VALUES (1,"
							+ customer + ");";
					rs = stmt.executeQuery(sql);

					stmt = con.createStatement();
					sql = "INSERT INTO review_tbl (reviewid,asin,rate,votes,helpful,entrydate,customerid) VALUES ('"
							+ reviewid
							+ "','"
							+ idString
							+ "',"
							+ rating
							+ ","
							+ votes
							+ ","
							+ helpful
							+ ",'"
							+ reviewdate
							+ "','"
							+ customer + "');";
					rs = stmt.executeQuery(sql);

					rs.close();

				}
			}

			// データベースのクローズ
			stmt.close();
			con.close();

		} catch (SQLException e) {
			System.err.println("SQL failed.");
			e.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
	}

	private void saveCustom(String idString) {
	}

	synchronized public void processStart() {
		signal = 1;
		System.out.println("ccc");
		infoCollect.bar.setIndeterminate(true);
		infoCollect.lblState.setText("データ収集中");
		
	}

	synchronized public void processPause() {
		signal = 0;
		infoCollect.lblState.setText("処理中断中");
		infoCollect.bar.setIndeterminate(false);
	}

}
