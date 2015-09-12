import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//import javax.lang.model.util.Elements;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class USamazonCrawler extends Thread {
	private int signal = 0;
	private DefaultListModel<String> listModel;

	private JProgressBar bar;
	private JLabel lblState;
	private String driver;
	private String server;
	private String dbname;
	private String url;
	private String user;
	private String password;

	public USamazonCrawler(JProgressBar bar, JLabel lblState,
			DefaultListModel<String> listModel) {
		// TODO 自動生成されたコンストラクター・スタブ
		this.listModel = listModel;
		this.bar = bar;
		this.lblState = lblState;

		// JDBCドライバの登録
		driver = "org.postgresql.Driver";
		// データベースの指定
		server = "localhost";
		dbname = "db_usamazon";
		url = "jdbc:postgresql://" + server + "/" + dbname;
		user = "postgres";
		password = "wth050527";
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		//
		while (true) {
			// System.out.println("bbb");
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
					try {
						saveCustom(idString);
					} catch (IOException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}
				}
			}

		} catch (SQLException e) {
			System.err.println("SQL failed.");
			e.printStackTrace();
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
			/*
			 * // JDBCドライバの登録 String driver = "org.postgresql.Driver"; //
			 * データベースの指定 String server = "localhost"; // PostgreSQL サーバ ( IP または
			 * ホスト名 ) String dbname = "db_usamazon"; // データベース名 String url =
			 * "jdbc:postgresql://" + server + "/" + dbname; String user =
			 * "postgres"; // データベース作成ユーザ名 String password = "wth050527"; //
			 * データベース作成ユーザパスワード Class.forName(driver);
			 */
			// データベースとの接続
			Connection con = DriverManager.getConnection(url, user, password);
			// テーブル照会実行
			Statement stmt = con.createStatement();
			String sql = "";
			ResultSet rs;
			Boolean redun = false;

			sql = "SELECT COUNT(*) FROM worklist_tbl WHERE targetid='"
					+ idString + "';";
			rs = stmt.executeQuery(sql);
			rs.next();
			if (rs.getInt(1) != 0) {
				redun = true;
			}

			stmt = con.createStatement();
			sql = "SELECT COUNT(*) FROM item_tbl WHERE asin='" + idString
					+ "';";
			rs = stmt.executeQuery(sql);
			rs.next();
			if (rs.getInt(1) != 0) {
				redun = true;
			}

			if (redun == false) {

				Date date = new Date();
				listModel.add(0, date.toString() + "Amazon.comから取得:アイテム - "
						+ idString);

				Elements breadcrumbs = document1.getElementById(
						"wayfinding-breadcrumbs_feature_div").getElementsByTag(
						"li");
				ArrayList<String> cats = new ArrayList<String>(); // カテゴリー群
				for (Element elem : breadcrumbs) {
					String tmp = elem.text();
					if (tmp.equals(">") == false) {
						cats.add(tmp);
					}
				}
				Element entrydate = document1
						.getElementsByClass("date-first-available").first()
						.children().last(); // 登録日

				switch (cats.size()) {
				case 1:
					sql = "INSERT INTO item_tbl (asin,cat1,entrydate) VALUES ('"
							+ idString
							+ "','"
							+ cats.get(0)
							+ "','"
							+ entrydate.text() + "');";
					break;

				case 2:
					sql = "INSERT INTO item_tbl (asin,cat1,cat2,entrydate) VALUES ('"
							+ idString
							+ "','"
							+ cats.get(0)
							+ "','"
							+ cats.get(1) + "','" + entrydate.text() + "');";
					break;

				case 3:
					sql = "INSERT INTO item_tbl (asin,cat1,cat2,cat3,entrydate) VALUES ('"
							+ idString
							+ "','"
							+ cats.get(0)
							+ "','"
							+ cats.get(1)
							+ "','"
							+ cats.get(2)
							+ "','"
							+ entrydate.text() + "');";
					break;
				case 4:
					sql = "INSERT INTO item_tbl (asin,cat1,cat2,cat3,cat4,entrydate) VALUES ('"
							+ idString
							+ "','"
							+ cats.get(0)
							+ "','"
							+ cats.get(1)
							+ "','"
							+ cats.get(2)
							+ "','"
							+ cats.get(3) + "','" + entrydate.text() + "');";
					break;
				case 5:
					sql = "INSERT INTO item_tbl (asin,cat1,cat2,cat3,cat4,cat5,entrydate) VALUES ('"
							+ idString
							+ "','"
							+ cats.get(0)
							+ "','"
							+ cats.get(1)
							+ "','"
							+ cats.get(2)
							+ "','"
							+ cats.get(3)
							+ "','"
							+ cats.get(4)
							+ "','"
							+ entrydate.text() + "');";
					break;
				default:
					break;
				}

				rs = stmt.executeQuery(sql);

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
								.getElementsByClass("review-date").get(0)
								.text().substring(2); // 投稿日

						String vote_help_senten = element
								.getElementsByClass("helpful-votes-count")
								.get(0).text();

						listModel.add(0, date.toString()
								+ "Amazon.comから取得:レビュー - " + reviewid);

						Pattern p = Pattern.compile("[0-9]+");
						Matcher m = p.matcher(vote_help_senten);

						m.find();
						int helpful = Integer.parseInt(m.group()); // 役立ち人数
						m.find();
						int votes = Integer.parseInt(m.group()); // 投票総数

						// 作業リストに存在するか検査
						Boolean redun1 = false;
						stmt = con.createStatement();
						sql = "SELECT COUNT(*) FROM worklist_tbl WHERE targetid='"
								+ customer + "';";
						rs = stmt.executeQuery(sql);
						rs.next();
						if (rs.getInt(1) != 0) {
							redun1 = true;
						}

						// 重複なければテーブル書き込み実行
						if (redun1 == false) {
							stmt = con.createStatement();
							sql = "INSERT INTO worklist_tbl (class,targetid) VALUES (1,"
									+ customer + ");";
							rs = stmt.executeQuery(sql);
						}

						// レビュー重複確認
						Boolean redun2 = false;
						stmt = con.createStatement();
						sql = "SELECT COUNT(*) FROM review_tbl WHERE reviewid='"
								+ reviewid + "';";
						rs = stmt.executeQuery(sql);
						rs.next();
						if (rs.getInt(1) != 0) {
							redun2 = true;
						}

						// 重複なければレビューを追加
						if (redun2 == false) {
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
									+ customer
									+ "');";
							rs = stmt.executeQuery(sql);
						}

						rs.close();

					}
				}
			}

			// データベースのクローズ
			stmt.close();
			con.close();

		} catch (SQLException e) {
			System.err.println("SQL failed.");
			e.printStackTrace();
		}
	}

	private void saveCustom(String idString) throws IOException {
		Document tmppage = Jsoup
				.connect(
						"http://www.amazon.com/gp/cdp/member-reviews/"
								+ idString
								+ "?ie=UTF8&display=public&page=1&sort_by=MostRecentReview")
				.get();

		String reviewCountStr = tmppage.getElementsByClass("small").get(2)
				.text();

		Pattern p = Pattern.compile("[0-9]+");
		Matcher m = p.matcher(reviewCountStr);

		m.find();
		int reviewCount = Integer.parseInt(m.group()); // レビュー数

		ArrayList<Document> cusReviewpage = new ArrayList<Document>();
		for (int i = 0; i < Math.ceil(reviewCount * 0.1); i++) {
			cusReviewpage.add(Jsoup.connect(
					"http://www.amazon.com/gp/cdp/member-reviews/" + idString
							+ "?ie=UTF8&display=public&page=" + (i + 1)
							+ "&sort_by=MostRecentReview").get());
		}

		for (Document document : cusReviewpage) {
			
		}

	}

	synchronized public void processStart() {
		signal = 1;
		System.out.println("ccc");
		bar.setIndeterminate(true);
		lblState.setText("データ収集中");

	}

	synchronized public void processPause() {
		signal = 0;
		lblState.setText("処理中断中");
		bar.setIndeterminate(false);
	}

}
