////////////////////////////////////////////////////////////////////////////////
//  Copyright 2021 Cosgy Dev                                                   /
//                                                                             /
//     Licensed under the Apache License, Version 2.0 (the "License");         /
//     you may not use this file except in compliance with the License.        /
//     You may obtain a copy of the License at                                 /
//                                                                             /
//        http://www.apache.org/licenses/LICENSE-2.0                           /
//                                                                             /
//     Unless required by applicable law or agreed to in writing, software     /
//     distributed under the License is distributed on an "AS IS" BASIS,       /
//     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied./
//     See the License for the specific language governing permissions and     /
//     limitations under the License.                                          /
////////////////////////////////////////////////////////////////////////////////

package dev.cosgy.TextToSpeak.audio;

import dev.cosgy.TextToSpeak.Bot;
import dev.cosgy.TextToSpeak.utils.OtherUtil;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.*;
import java.util.HashMap;
import java.util.List;

public class Dictionary {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Bot bot;
    private Path path = null;
    private boolean create = false;
    private Connection connection;
    private Statement statement;

    /**
     * Long サーバーID
     * String 1　元の単語
     * String 2　単語の読み
     */
    private HashMap<Long, HashMap<String, String>> guildDic;

    public Dictionary(Bot bot) {
        this.bot = bot;
        this.guildDic = new HashMap<>();
    }

    /**
     * クラスを初期化するためのメゾット
     */
    public void Init() {
        int count = 0;
        logger.info("辞書データの読み込みを開始");
        path = OtherUtil.getPath("UserData.sqlite");
        if (!path.toFile().exists()) {
            create = true;
            String original = OtherUtil.loadResource(this, "UserData.sqlite");
            try {
                FileUtils.writeStringToFile(path.toFile(), original, StandardCharsets.UTF_8);
                logger.info("データベースファイルが存在しなかったためファイルを作成しました。");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:UserData.sqlite");
            statement = connection.createStatement();
            String SQL = "CREATE TABLE IF NOT EXISTS Dictionary(guild_id integer,word text,reading)";
            statement.execute(SQL);

            List<Guild> guilds = bot.getJDA().getGuilds();


            for (Guild value : guilds) {
                long guildId = value.getIdLong();
                PreparedStatement ps = connection.prepareStatement("select * from Dictionary where guild_id = ?");
                ps.setLong(1, guildId);
                ResultSet rs = ps.executeQuery();
                HashMap<String, String> word = new HashMap<>();
                while (rs.next()) {
                    word.put(rs.getString(2), rs.getString(3));
                    count++;
                }
                guildDic.put(guildId, word);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        logger.info("辞書データの読み込み完了 単語数:" + count);
    }

    /**
     * データベースとHashMapの内容を更新または新規追加します。
     *
     * @param guildId サーバーID
     * @param word    単語
     * @param reading 読み方
     */
    public void UpdateDictionary(Long guildId, String word, String reading) {
        HashMap<String, String> words;
        words = bot.getDictionary().GetWords(guildId);
        boolean NewWord = false;
        try {
            NewWord = words.containsKey(word);
            words.put(word, reading);
        } catch (NullPointerException e) {
            words = new HashMap<>();
            words.put(word, reading);
        }

        guildDic.put(guildId, words);
        String sql;
        PreparedStatement ps;
        if (!NewWord) {
            sql = "INSERT INTO Dictionary VALUES (?,?,?)";
            try {
                ps = connection.prepareStatement(sql);
                ps.setLong(1, guildId);
                ps.setString(2, word);
                ps.setString(3, reading);
                ps.execute();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } else {
            sql = "UPDATE Dictionary SET reading = ? WHERE guild_id = ? AND word = ?";
            try {
                ps = connection.prepareStatement(sql);
                ps.setLong(2, guildId);
                ps.setString(3, word);
                ps.setString(1, reading);
                ps.execute();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    /**
     * データベースに登録されている単語を削除します。
     *
     * @param guildId サーバーID
     * @param word    単語
     * @return 正常に削除できた場合は {@code true}、削除時に問題が発生した場合は{@code false}を返します。
     */
    public boolean DeleteDictionary(Long guildId, String word) {
        HashMap<String, String> words;
        words = bot.getDictionary().GetWords(guildId);
        try {
            words.remove(word);
        } catch (NullPointerException e) {
            return false;
        }
        guildDic.put(guildId, words);

        String sql = "DELETE FROM Dictionary WHERE guild_id = ? AND word = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, guildId);
            ps.setString(2, word);
            ps.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return true;
    }

    /**
     * サーバーの辞書データを取得します。
     *
     * @param guildId サーバーID
     * @return {@code HashMap<String, String>}形式の変数を返します。
     */
    public HashMap<String, String> GetWords(Long guildId) {
        return guildDic.get(guildId);
    }
}
