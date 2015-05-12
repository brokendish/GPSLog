# GPSLog
GPSから緯度経度、高度、時間を取得してリストに表示する。普段生活していて自分がどのくらいの高度（標高）に居るかってあまり気にしていなかったので、高度（標高）が分かるだけでもいいかなと思ったのですが、今のところ次のようになっています。

 

★作成が済んでいる部分
・GPSの情報を取得（緯度経度、高度、時間）
・取得した情報をリストに表示する（緯度経度から住所を逆引きする）
・リストをタッチしたらGoogleMapでその場所を表示する（座標をGoogleMapに渡して表示するだけ）
・リストを長押しでそのリストの行を削除する
・リストの状態はローカルファイルに保存する
※高度（標高）の情報が欲しかったので、GPSプロバイダからの情報のみ取得
（NETWORKプロバーダからだと標高が取れないので）

★ここから下はそのうちやりたいこと
・ローカルファイルのエクスポート、インポート（メール添付か、GoogleDrive等のネットワークステレージ）
・設定画面の作成、高度（標高）専用の表示画面
・リストを複数選択して、Map上に複数のピンを立てる
・リストのデータから条件を指定して一致したもののみ表示する
