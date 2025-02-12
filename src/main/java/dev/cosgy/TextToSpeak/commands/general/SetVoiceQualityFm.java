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

package dev.cosgy.TextToSpeak.commands.general;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import dev.cosgy.TextToSpeak.Bot;
import dev.cosgy.TextToSpeak.settings.UserSettings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SetVoiceQualityFm extends SlashCommand {
    protected Bot bot;

    public SetVoiceQualityFm(Bot bot) {
        this.bot = bot;
        this.name = "setqfm";
        this.help = "追加ハーフトーンの設定を変更します。";
        this.guildOnly = false;
        this.category = new Category("設定");

        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.STRING, "value", "0.1~100.0", true));

        this.options = options;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String args = event.getOption("value").getAsString();
        boolean result;
        float value = 0.0f;
        BigDecimal bd = null;
        try {
            //value = Float.parseFloat(args);
            bd = new BigDecimal(args);
            result = true;
        } catch (NumberFormatException e) {
            result = false;
        }
        if (!result) {
            event.reply("数値を設定して下さい。").queue();
            return;
        }

        BigDecimal min = new BigDecimal("0.0");
        BigDecimal max = new BigDecimal("100.0");

        //if(!(0.1f <= value && value <= 100.0f)){
        if (!(min.compareTo(bd) < 0 && max.compareTo(bd) > 0)) {
            event.reply("有効な数値を設定して下さい。0.1~100.0").queue();
            return;
        }
        UserSettings settings = bot.getUserSettingsManager().getSettings(event.getUser().getIdLong());
        settings.setVoiceQualityFm(bd.floatValue());
        event.reply("追加ハーフトーンを" + bd + "に設定しました。").queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty() && event.getMessage().getAttachments().isEmpty()) {
            EmbedBuilder ebuilder = new EmbedBuilder()
                    .setTitle("setqfmコマンド")
                    .addField("使用方法:", name + " <数値(0.0~)>", false)
                    .addField("説明:", "追加ハーフトーンの設定を変更します。", false);
            event.reply(ebuilder.build());
            return;
        }
        String args = event.getArgs();
        boolean result;
        float value = 0.0f;
        BigDecimal bd = null;
        try {
            //value = Float.parseFloat(args);
            bd = new BigDecimal(args);
            result = true;
        } catch (NumberFormatException e) {
            result = false;
        }
        if (!result) {
            event.reply("数値を設定して下さい。");
            return;
        }

        BigDecimal min = new BigDecimal("0.0");
        BigDecimal max = new BigDecimal("100.0");

        //if(!(0.1f <= value && value <= 100.0f)){
        if (!(min.compareTo(bd) < 0 && max.compareTo(bd) > 0)) {
            event.reply("有効な数値を設定して下さい。0.1~100.0");
            return;
        }
        UserSettings settings = bot.getUserSettingsManager().getSettings(event.getAuthor().getIdLong());
        settings.setVoiceQualityFm(bd.floatValue());
        event.reply("追加ハーフトーンを" + bd + "に設定しました。");
    }
}
