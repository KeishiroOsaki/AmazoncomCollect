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
			// System.out.println(signal);

			if (signal == 1) {
				// System.out.println("aaa");
				if (popfromQueue() == false) {
					processPause();
				}

			} else if (signal == 0) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
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

		System.out.println("キューをチェックします");

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
			System.out.println("キュー数" + rs.getInt(1));
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
				System.out.println("id：" + id);
				System.out.println("class：" + ic_class);
				System.out.println("targetid：" + idString);

				Boolean proResult = false;

				do {

					if (ic_class == 0) {
						try {
							saveItem(idString);
							proResult = true;
						} catch (IOException e) {
							// TODO 自動生成された catch ブロック
							e.printStackTrace();

						} catch (NullPointerException e) {
							// TODO: handle exception
							System.err.println("ヌルポ");
							e.printStackTrace();
						} catch (IndexOutOfBoundsException e) {
							// TODO: handle exception
							System.err.println("インデックスが超越");
							e.printStackTrace();
						}
					} else if (ic_class == 1) {

						try {
							saveCustom(idString);
							proResult = true;

						} catch (IOException e) {
							// TODO 自動生成された catch ブロック
							e.printStackTrace();
						} catch (NullPointerException e) {
							// TODO: handle exception
							System.err.println("ヌルポ");
							e.printStackTrace();
						} catch (IndexOutOfBoundsException e) {
							// TODO: handle exception
							System.err.println("インデックスが超越");
							e.printStackTrace();
						}

					}
				} while (proResult == false);
				// 読みだしたレコードを削除
				stmt = con.createStatement();
				sql = "DELETE FROM worklist_tbl WHERE id = " + id + ";";
				/* rs = */
				int kekka = stmt.executeUpdate(sql);
				System.out.println("消去したID：" + id);

				// データベースのクローズ
				rs.close();
				stmt.close();
				con.close();

			}

		} catch (SQLException e) {
			System.err.println("SQL failed.");
			e.printStackTrace();
		}

		return res;

	}

	private void saveItem(String idString) throws IOException {

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

			/*
			 * sql = "SELECT COUNT(*) FROM worklist_tbl WHERE targetid='" +
			 * idString + "';"; rs = stmt.executeQuery(sql); rs.next(); if
			 * (rs.getInt(1) != 0) { redun = true; }
			 */

			stmt = con.createStatement();
			sql = "SELECT COUNT(*) FROM item_tbl WHERE asin='" + idString
					+ "';";
			rs = stmt.executeQuery(sql);
			rs.next();
			if (rs.getInt(1) != 0) {
				redun = true;
			}

			// idがitem_tblに登録されてない時の処理
			if (redun == false) {

				System.out.println("アイテム：" + idString + "を取得します");
				Document document1 = Jsoup
						.connect("http://www.amazon.com/dp/" + idString)
						.followRedirects(true).timeout(0)
						.userAgent("Mozilla/5.0").get();
				Document tmpReviewPage = Jsoup
						.connect(
								"http://www.amazon.com/product-reviews/"
										+ idString
										+ "/ref=cm_cr_pr_viewopt_srt?ie=UTF8&showViewpoints=1&sortBy=recent&reviewerType=all_reviews&formatType=all_formats&filterByStar=all_stars&pageNumber=1")
						.followRedirects(true).timeout(0)
						.userAgent("Mozilla/5.0").get();
				Element revcount = tmpReviewPage.getElementsByClass(
						"totalReviewCount").get(0);
				int totalReviewCount = Integer.parseInt(revcount.text()
						.replaceAll(",", ""));
				System.out.println("レビュー数：" + totalReviewCount);
				ArrayList<Document> revpagelist = new ArrayList<Document>();
				// ArrayList<String> customers = new ArrayList<String>();
				for (int i = 0; i < Math.ceil(totalReviewCount * 0.1); i++) {
					revpagelist
							.add(Jsoup
									.connect(
											"http://www.amazon.com/product-reviews/"
													+ idString
													+ "/ref=cm_cr_pr_viewopt_srt?ie=UTF8&showViewpoints=1&sortBy=recent&reviewerType=all_reviews&formatType=all_formats&filterByStar=all_stars&pageNumber="
													+ (i + 1))
									.followRedirects(true).timeout(0)
									.userAgent("Mozilla/5.0").get());
					Date date = new Date();
					listModel.add(
							0,
							date.toString() + " レビューページ@アイテム取得中：" + idString
									+ "(" + (i + 1) + "/"
									+ Math.ceil(totalReviewCount * 0.1) + ")");
				}

				Date date = new Date();
				listModel.add(0, date.toString() + "Amazon.comから取得:アイテム - "
						+ idString);
				stmt = con.createStatement();
				// try {

				String productTitle;
				if (document1.getElementById("productTitle") != null) {
					productTitle = document1.getElementById("productTitle")
							.text().replaceAll("'", "");
				} else {
					productTitle = document1.getElementById("btAsinTitle")
							.text().replaceAll("'", "");

				}

				ArrayList<String> cats = new ArrayList<String>(); // カテゴリー群

				Elements breadcrumbs = new Elements();
				try {
				breadcrumbs = document1.getElementById(
						"wayfinding-breadcrumbs_feature_div").getElementsByTag(
						"li");
				} catch( NullPointerException e) {} 
				
				for (Element elem : breadcrumbs) {
					String tmp = elem.text();
					if (tmp.trim().equals("›") == false) {
						cats.add(tmp.trim());
					}
				}

				System.out.println(cats.toString());
				/*
				 * String entrydate = "'0'"; try {
				 * 
				 * if (document1 .getElementsByClass("date-first-available")
				 * .isEmpty() == false) { entrydate = document1
				 * .getElementsByClass("date-first-available")
				 * .first().children().last().text(); // 登録日 } else { String
				 * chikanmoto = document1 .getElementsByClass("content").last()
				 * .getElementsByTag("li").last().html(); Pattern p3 =
				 * Pattern.compile("<b>.*</b>"); Matcher m3 =
				 * p3.matcher(chikanmoto); entrydate = m3.replaceAll(""); if
				 * (entrydate.length() > 20) { entrydate = "'0'"; } } } catch
				 * (NullPointerException e) { // TODO: handle exception }
				 */

				switch (cats.size()) {
				case 0:
					sql = "INSERT INTO item_tbl (asin,producttitle) VALUES ('"
							+ idString + "',E'" + productTitle + "');";
					break;
				case 1:
					sql = "INSERT INTO item_tbl (asin,cat1,producttitle) VALUES ('"
							+ idString
							+ "','"
							+ cats.get(0)
							+ "',E'"
							+ productTitle + "');";
					break;

				case 2:
					sql = "INSERT INTO item_tbl (asin,cat1,cat2,producttitle) VALUES ('"
							+ idString
							+ "','"
							+ cats.get(0)
							+ "','"
							+ cats.get(1) + "',E'" + productTitle + "');";
					break;

				case 3:
					sql = "INSERT INTO item_tbl (asin,cat1,cat2,cat3,producttitle) VALUES ('"
							+ idString
							+ "','"
							+ cats.get(0)
							+ "','"
							+ cats.get(1)
							+ "','"
							+ cats.get(2)
							+ "',E'"
							+ productTitle + "');";
					break;
				case 4:
					sql = "INSERT INTO item_tbl (asin,cat1,cat2,cat3,cat4,producttitle) VALUES ('"
							+ idString
							+ "','"
							+ cats.get(0)
							+ "','"
							+ cats.get(1)
							+ "','"
							+ cats.get(2)
							+ "','"
							+ cats.get(3) + "',E'"

							+ productTitle + "');";
					break;
				default:
					sql = "INSERT INTO item_tbl (asin,cat1,cat2,cat3,cat4,cat5,producttitle) VALUES ('"
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
							+ "',E'"
							+ productTitle + "');";
					break;
				/*
				 * default: sql = "";
				 * System.err.println("実行を想定していない箇所が実行されました！"); break;
				 */
				}
				/*
				 * } catch (NullPointerException e) { // TODO: handle exception
				 * sql = "INSERT INTO item_tbl (asin) VALUES ('" + idString +
				 * "');"; }
				 */

				int kekka = stmt.executeUpdate(sql);

				for (Document d : revpagelist) {

					Elements tmpElements = d.getElementsByClass("review");
					for (Element element : tmpElements) {
						try {
							int rating = Character.getNumericValue(element
									.getElementsByClass("a-icon-alt").get(0)
									.text().charAt(0)); // 星の数
							String customer;
							try {
								customer = element.getElementsByClass("author")
										.get(0).attr("href").split("/")[4]; // 投稿者ID
							} catch (NullPointerException e) {
								// TODO: handle exception
								continue;
							} catch (IndexOutOfBoundsException e) {
								// TODO: handle exception
								continue;
							}

							String reviewid = element.attr("id"); // レビューID
							String reviewdate = element
									.getElementsByClass("review-date").get(0)
									.text().substring(2); // 投稿日

							String vote_help_senten = element
									.getElementsByClass("helpful-votes-count")
									.get(0).text();

							date = new Date();
							listModel.add(0, date.toString()
									+ "Amazon.comから取得:レビュー - " + reviewid);
							int helpful = 0;
							int votes = 0;

							if (vote_help_senten.trim().equals("") == false) {
								Pattern p = Pattern.compile("[0-9]+");
								Matcher m = p.matcher(vote_help_senten);

								m.find();
								helpful = Integer.parseInt(m.group()); // 役立ち人数
								m.find();
								votes = Integer.parseInt(m.group()); // 投票総数
							}

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
								sql = "INSERT INTO worklist_tbl (class,targetid) VALUES (1,'"
										+ customer + "');";
								kekka = stmt.executeUpdate(sql);
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
								kekka = stmt.executeUpdate(sql);
							}
						} catch (NullPointerException e) {
							// TODO: handle exception
							continue;
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
		} catch (NullPointerException e) {
			// TODO: handle exception
			System.err.println("ヌルポでした in saveItem");
			e.printStackTrace();
		}
	}

	private void saveCustom(String idString) throws IOException,
			NullPointerException, IndexOutOfBoundsException {
		System.out.println("カスタマー：" + idString + "を取得します");
		// 重複確認
		Boolean redun3 = false;

		try {
			Connection con = DriverManager.getConnection(url, user, password);

			Statement stmt = con.createStatement();
			String sql = "SELECT COUNT(*) FROM customer_tbl WHERE customerid='"
					+ idString + "';";
			ResultSet rs = stmt.executeQuery(sql);
			rs.next();
			if (rs.getInt(1) != 0) {
				redun3 = true;
			}

			if (redun3 == false) {

				// 1ページ目を取得しカスタマーのレビュー数をget
				Document tmppage = Jsoup
						.connect(
								"http://www.amazon.com/gp/cdp/member-reviews/"
										+ idString
										+ "?ie=UTF8&display=public&page=1&sort_by=MostRecentReview")
						.followRedirects(true).timeout(0)
						.userAgent("Mozilla/5.0").get();

				String reviewCountStr = tmppage.getElementsByClass("small")
						.get(2).text();

				Pattern p = Pattern.compile("[0-9]+");
				Matcher m = p.matcher(reviewCountStr);

				m.find();
				int reviewCount = Integer.parseInt(m.group()); // レビュー数
				System.out.println("レビュー数：" + reviewCount);

				// System.out.println("span first：" +
				// tmppage/*.getElementsByClass("first")*/.toString());
				String cus_name = tmppage.getElementsByClass("first").first()
						.text();
				System.out.println("名前が含まれているはずの文字列：" + cus_name);
				Matcher m2 = Pattern.compile(".+(?=\')").matcher(cus_name);
				m2.find();
				cus_name = m2.group();
				System.out.println("名前:" + cus_name);

				stmt = con.createStatement();
				sql = "INSERT INTO customer_tbl (customerid,customername) VALUES ('"
						+ idString + "','" + cus_name + "');";
				int kekka = stmt.executeUpdate(sql);

				Date date = new Date();
				listModel.add(0, date.toString() + " Amazon.comから取得:カスタマー - "
						+ idString);

				// カスタマーのレビューページすべてを一気に取得
				ArrayList<Document> cusReviewpage = new ArrayList<Document>();
				for (int i = 0; i < Math.ceil(reviewCount * 0.1); i++) {
					cusReviewpage.add(Jsoup
							.connect(
									"http://www.amazon.com/gp/cdp/member-reviews/"
											+ idString
											+ "?ie=UTF8&display=public&page="
											+ (i + 1)
											+ "&sort_by=MostRecentReview")
							.followRedirects(true).timeout(0)
							.userAgent("Mozilla/5.0").get());
					date = new Date();
					listModel.add(
							0,
							date.toString() + " レビューページ@カスタマー取得中：" + idString
									+ "(" + (i + 1) + "/"
									+ Math.ceil(reviewCount * 0.1) + ")");
				}

				// レビューページから商品の一覧を取得
				ArrayList<String> itemList = new ArrayList<String>();
				Pattern p1 = Pattern.compile(".*/dp/(B\\w{9}).*");
				System.out.println(itemList.toString());
				// System.out.println(cusReviewpage.toString());

				for (Document document : cusReviewpage) {
					for (Element elem : document.getElementsByTag("a")) {

						if (elem.attr("href").isEmpty() == false) {
							// 判定するパターンを生成
							// System.out.println(elem.attr("href"));

							Matcher m1 = p1.matcher(elem.attr("href"));

							if (m1.matches()) {
								// System.out.println(m.group(0)); //
								// →abc123def456ghi
								// System.out.println(m.groupCount()); // →3

								// すでに抽出したアイテムとの重複確認
								if (strMatch(itemList, m1.group(1)) == false) {
									System.out.println("アイテム：" + m1.group(1));
									itemList.add(m1.group(1));
									date = new Date();
									listModel.add(0, date.toString()
											+ " アイテム抽出：" + m1.group(1));

									try {
										// データベースとの接続
										con = DriverManager.getConnection(url,
												user, password);
										// テーブル照会実行
										stmt = con.createStatement();
										sql = "";
										// ResultSet rs;
										Boolean redun = false;

										// 作業リストにあるかないか確認
										sql = "SELECT COUNT(*) FROM worklist_tbl WHERE targetid='"
												+ m1.group(1) + "';";
										rs = stmt.executeQuery(sql);
										rs.next();
										if (rs.getInt(1) != 0) {
											redun = true;
										}

										// 商品リストの有無を確認
										stmt = con.createStatement();
										sql = "SELECT COUNT(*) FROM item_tbl WHERE asin='"
												+ m1.group(1) + "';";
										rs = stmt.executeQuery(sql);
										rs.next();
										if (rs.getInt(1) != 0) {
											redun = true;
										}
										// 両方とも重複なければ作業リストに投げる
										if (redun == false) {
											stmt = con.createStatement();
											sql = "INSERT INTO worklist_tbl (class,targetid) VALUES (0,'"
													+ m1.group(1) + "');";
											kekka = stmt.executeUpdate(sql);
											rs.close();

										}

										// データベースのクローズ
										stmt.close();
										con.close();

									} catch (SQLException e) {
										System.err.println("SQL failed.");
										e.printStackTrace();
									}
								}
							}
						}

					}
				}
			}
		} catch (SQLException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
	}

	static boolean strMatch(ArrayList<String> arr, String str) {
		for (String string : arr) {
			if (string.equals(str)) {
				return true;
			}
		}
		return false;
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
