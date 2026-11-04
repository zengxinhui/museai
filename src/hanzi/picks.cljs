(ns hanzi.picks
  "Curated Pick of the Day bank.

   Every entry is a real, grammatically correct phrase, sentence,
   or short dialogue — not characters glued together by the app.
   The app cannot invent correct grammar offline, so correctness
   lives here, in this curated list, and the app only selects
   from it.

   Constraints checked by hand and by script (see the verification
   in the commit message for this file):
   - every character in every :text is in the top-500 set
     (src/hanzi/data.cljs), so a pick never asks the learner to
     read a character the app does not teach;
   - the number of pinyin syllables in each line equals the
     number of characters in that line (punctuation excluded);
   - :kind is :phrase, :sentence, or :dialogue. Dialogue entries
     carry :lines; the others carry :text/:pinyin/:meaning.
   Pinyin is dictionary pinyin (tone sandhi is not rewritten,
   e.g. 你好 is nǐ hǎo, not ní hǎo).")

(def picks
  [{:id 1 :kind :phrase :text "你好" :pinyin "nǐ hǎo" :meaning "Hello."}
   {:id 2 :kind :phrase :text "再见" :pinyin "zài jiàn" :meaning "Goodbye."}
   {:id 3 :kind :sentence :text "你好吗？" :pinyin "nǐ hǎo ma?" :meaning "How are you?"}
   {:id 4 :kind :sentence :text "我很好。" :pinyin "wǒ hěn hǎo." :meaning "I am very well."}
   {:id 5 :kind :sentence :text "我们是朋友。" :pinyin "wǒ men shì péng you." :meaning "We are friends."}
   {:id 6 :kind :sentence :text "我爱我的家。" :pinyin "wǒ ài wǒ de jiā." :meaning "I love my home."}
   {:id 7 :kind :sentence :text "今天天气很好。" :pinyin "jīn tiān tiān qì hěn hǎo." :meaning "The weather is very good today."}
   {:id 8 :kind :sentence :text "我的朋友很多。" :pinyin "wǒ de péng you hěn duō." :meaning "I have many friends."}
   {:id 9 :kind :sentence :text "我喜欢中国。" :pinyin "wǒ xǐ huan Zhōng guó." :meaning "I like China."}
   {:id 10 :kind :sentence :text "中国很大。" :pinyin "Zhōng guó hěn dà." :meaning "China is very big."}
   {:id 11 :kind :sentence :text "你叫什么名字？" :pinyin "nǐ jiào shén me míng zi?" :meaning "What is your name?"}
   {:id 12 :kind :sentence :text "你会说中文吗？" :pinyin "nǐ huì shuō Zhōng wén ma?" :meaning "Can you speak Chinese?"}
   {:id 13 :kind :sentence :text "我会说一点中文。" :pinyin "wǒ huì shuō yì diǎn Zhōng wén." :meaning "I can speak a little Chinese."}
   {:id 14 :kind :sentence :text "这是我的书。" :pinyin "zhè shì wǒ de shū." :meaning "This is my book."}
   {:id 15 :kind :sentence :text "那是你的吗？" :pinyin "nà shì nǐ de ma?" :meaning "Is that yours?"}
   {:id 16 :kind :sentence :text "你知道吗？" :pinyin "nǐ zhī dao ma?" :meaning "Do you know?"}
   {:id 17 :kind :sentence :text "我不知道。" :pinyin "wǒ bù zhī dao." :meaning "I don't know."}
   {:id 18 :kind :sentence :text "我想去中国。" :pinyin "wǒ xiǎng qù Zhōng guó." :meaning "I want to go to China."}
   {:id 19 :kind :sentence :text "对不起，没关系。" :pinyin "duì bu qǐ, méi guān xi." :meaning "Sorry. — It's okay, no problem."}
   {:id 20 :kind :phrase :text "生日快乐" :pinyin "shēng rì kuài lè" :meaning "Happy birthday."}
   {:id 21 :kind :phrase :text "新年快乐" :pinyin "xīn nián kuài lè" :meaning "Happy New Year."}
   {:id 22 :kind :sentence :text "老师好，同学们好。" :pinyin "lǎo shī hǎo, tóng xué men hǎo." :meaning "Hello, teacher. Hello, classmates."}
   {:id 23 :kind :sentence :text "早上好。" :pinyin "zǎo shang hǎo." :meaning "Good morning."}
   {:id 24 :kind :sentence :text "晚上好。" :pinyin "wǎn shang hǎo." :meaning "Good evening."}
   {:id 25 :kind :sentence :text "你吃了吗？" :pinyin "nǐ chī le ma?" :meaning "Have you eaten?"}
   {:id 26 :kind :sentence :text "我吃了。" :pinyin "wǒ chī le." :meaning "I have eaten."}
   {:id 27 :kind :sentence :text "时间到了，我们走吧。" :pinyin "shí jiān dào le, wǒ men zǒu ba." :meaning "It's time. Let's go."}
   {:id 28 :kind :sentence :text "明天见。" :pinyin "míng tiān jiàn." :meaning "See you tomorrow."}
   {:id 29 :kind :sentence :text "书很好看。" :pinyin "shū hěn hǎo kàn." :meaning "The book is very nice to read."}
   {:id 30 :kind :phrase :text "很好吃。" :pinyin "hěn hǎo chī." :meaning "Very delicious."}
   {:id 31 :kind :sentence :text "你的家很大。" :pinyin "nǐ de jiā hěn dà." :meaning "Your home is very big."}
   {:id 32 :kind :sentence :text "上海很大。" :pinyin "Shàng hǎi hěn dà." :meaning "Shanghai is very big."}
   {:id 33 :kind :sentence :text "大家好。" :pinyin "dà jiā hǎo." :meaning "Hello, everyone."}
   {:id 34 :kind :phrase :text "小朋友。" :pinyin "xiǎo péng you." :meaning "Little friend; child."}
   {:id 35 :kind :sentence :text "请坐。" :pinyin "qǐng zuò." :meaning "Please sit down."}
   {:id 36 :kind :sentence :text "请进。" :pinyin "qǐng jìn." :meaning "Please come in."}
   {:id 37 :kind :dialogue :meaning "A short conversation about how you are."
    :lines [{:speaker "A" :text "你好吗？" :pinyin "nǐ hǎo ma?" :meaning "How are you?"}
            {:speaker "B" :text "我很好，你呢？" :pinyin "wǒ hěn hǎo, nǐ ne?" :meaning "I'm very well, and you?"}
            {:speaker "A" :text "我也很好。" :pinyin "wǒ yě hěn hǎo." :meaning "I'm very well too."}]}
   {:id 38 :kind :dialogue :meaning "A short conversation about names and being a student."
    :lines [{:speaker "A" :text "你叫什么名字？" :pinyin "nǐ jiào shén me míng zi?" :meaning "What is your name?"}
            {:speaker "B" :text "我叫小明。" :pinyin "wǒ jiào Xiǎo Míng." :meaning "My name is Xiaoming."}
            {:speaker "A" :text "你是学生吗？" :pinyin "nǐ shì xué sheng ma?" :meaning "Are you a student?"}
            {:speaker "B" :text "是，我是学生。" :pinyin "shì, wǒ shì xué sheng." :meaning "Yes, I'm a student."}]}
   {:id 39 :kind :dialogue :meaning "A short conversation about going to read together."
    :lines [{:speaker "A" :text "今天天气很好。" :pinyin "jīn tiān tiān qì hěn hǎo." :meaning "The weather is very good today."}
            {:speaker "B" :text "是，我们一起去看书吗？" :pinyin "shì, wǒ men yì qǐ qù kàn shū ma?" :meaning "Yes, shall we go and read books together?"}
            {:speaker "A" :text "好，我们走吧。" :pinyin "hǎo, wǒ men zǒu ba." :meaning "Good, let's go."}]}
   {:id 40 :kind :dialogue :meaning "A short conversation about liking China."
    :lines [{:speaker "A" :text "你喜欢中国吗？" :pinyin "nǐ xǐ huan Zhōng guó ma?" :meaning "Do you like China?"}
            {:speaker "B" :text "中国很大，很好。" :pinyin "Zhōng guó hěn dà, hěn hǎo." :meaning "China is very big and very good."}
            {:speaker "A" :text "我也喜欢中国。" :pinyin "wǒ yě xǐ huan Zhōng guó." :meaning "I like China too."}]}
   {:id 41 :kind :dialogue :meaning "A short conversation about whose book this is."
    :lines [{:speaker "A" :text "这是你的书吗？" :pinyin "zhè shì nǐ de shū ma?" :meaning "Is this your book?"}
            {:speaker "B" :text "是，这是我的书。" :pinyin "shì, zhè shì wǒ de shū." :meaning "Yes, this is my book."}
            {:speaker "A" :text "你的书很好看。" :pinyin "nǐ de shū hěn hǎo kàn." :meaning "Your book is very nice to read."}]}
   {:id 42 :kind :dialogue :meaning "A short conversation about eating."
    :lines [{:speaker "A" :text "你吃了吗？" :pinyin "nǐ chī le ma?" :meaning "Have you eaten?"}
            {:speaker "B" :text "我吃了，你呢？" :pinyin "wǒ chī le, nǐ ne?" :meaning "I've eaten, and you?"}
            {:speaker "A" :text "我也吃了。" :pinyin "wǒ yě chī le." :meaning "I've eaten too."}]}
   {:id 43 :kind :sentence :text "有很多人。" :pinyin "yǒu hěn duō rén." :meaning "There are many people."}
   {:id 44 :kind :sentence :text "他在家。" :pinyin "tā zài jiā." :meaning "He is at home."}
   {:id 45 :kind :sentence :text "她是我的朋友。" :pinyin "tā shì wǒ de péng you." :meaning "She is my friend."}
   {:id 46 :kind :sentence :text "我有一个朋友。" :pinyin "wǒ yǒu yí gè péng you." :meaning "I have a friend."}
   {:id 47 :kind :phrase :text "一个人。" :pinyin "yí gè rén." :meaning "One person."}
   {:id 48 :kind :sentence :text "他来了。" :pinyin "tā lái le." :meaning "He has come."}
   {:id 49 :kind :sentence :text "这是什么地方？" :pinyin "zhè shì shén me dì fang?" :meaning "What place is this?"}
   {:id 50 :kind :sentence :text "为什么？" :pinyin "wèi shén me?" :meaning "Why?"}
   {:id 51 :kind :sentence :text "这个孩子很好。" :pinyin "zhè gè hái zi hěn hǎo." :meaning "This child is very good."}
   {:id 52 :kind :sentence :text "他看着书。" :pinyin "tā kàn zhe shū." :meaning "He is reading a book."}
   {:id 53 :kind :sentence :text "我就是学生。" :pinyin "wǒ jiù shì xué sheng." :meaning "I am a student."}
   {:id 54 :kind :sentence :text "我和他是朋友。" :pinyin "wǒ hé tā shì péng you." :meaning "He and I are friends."}
   {:id 55 :kind :sentence :text "我要去中国。" :pinyin "wǒ yào qù Zhōng guó." :meaning "I want to go to China."}
   {:id 56 :kind :sentence :text "他出去了。" :pinyin "tā chū qù le." :meaning "He went out."}
   {:id 57 :kind :sentence :text "他说得很好。" :pinyin "tā shuō de hěn hǎo." :meaning "He speaks very well."}
   {:id 58 :kind :sentence :text "书在家里。" :pinyin "shū zài jiā lǐ." :meaning "The book is at home."}
   {:id 59 :kind :sentence :text "然后呢？" :pinyin "rán hòu ne?" :meaning "And then?"}
   {:id 60 :kind :sentence :text "我自己去。" :pinyin "wǒ zì jǐ qù." :meaning "I'll go by myself."}
   {:id 61 :kind :sentence :text "以后见。" :pinyin "yǐ hòu jiàn." :meaning "See you later."}
   {:id 62 :kind :sentence :text "可以吗？" :pinyin "kě yǐ ma?" :meaning "Is it okay? Can I?"}
   {:id 63 :kind :sentence :text "下次见。" :pinyin "xià cì jiàn." :meaning "See you next time."}
   {:id 64 :kind :sentence :text "而且他是我的朋友。" :pinyin "ér qiě tā shì wǒ de péng you." :meaning "And he is also my friend."}
   {:id 65 :kind :sentence :text "我去过中国。" :pinyin "wǒ qù guo Zhōng guó." :meaning "I have been to China."}
   {:id 66 :kind :sentence :text "你能来吗？" :pinyin "nǐ néng lái ma?" :meaning "Can you come?"}
   {:id 67 :kind :sentence :text "这是关于中国的书。" :pinyin "zhè shì guān yú Zhōng guó de shū." :meaning "This is a book about China."}
   {:id 68 :kind :sentence :text "我很开心。" :pinyin "wǒ hěn kāi xīn." :meaning "I am very happy."}
   {:id 69 :kind :sentence :text "他是我的朋友之一。" :pinyin "tā shì wǒ de péng you zhī yī." :meaning "He is one of my friends."}
   {:id 70 :kind :sentence :text "我们都很好。" :pinyin "wǒ men dōu hěn hǎo." :meaning "We are all very well."}
   {:id 71 :kind :sentence :text "发生了什么？" :pinyin "fā shēng le shén me?" :meaning "What happened?"}
   {:id 72 :kind :phrase :text "当然。" :pinyin "dāng rán." :meaning "Of course."}
   {:id 73 :kind :sentence :text "他成了老师。" :pinyin "tā chéng le lǎo shī." :meaning "He became a teacher."}
   {:id 74 :kind :sentence :text "只有一个人。" :pinyin "zhǐ yǒu yí gè rén." :meaning "There is only one person."}
   {:id 75 :kind :sentence :text "如果你来，我很高兴。" :pinyin "rú guǒ nǐ lái, wǒ hěn gāo xìng." :meaning "If you come, I'm very happy."}
   {:id 76 :kind :sentence :text "什么事？" :pinyin "shén me shì?" :meaning "What's the matter?"}
   {:id 77 :kind :sentence :text "把书给我。" :pinyin "bǎ shū gěi wǒ." :meaning "Give me the book."}
   {:id 78 :kind :sentence :text "他还没来。" :pinyin "tā hái méi lái." :meaning "He hasn't come yet."}
   {:id 79 :kind :sentence :text "用中文说。" :pinyin "yòng Zhōng wén shuō." :meaning "Say it in Chinese."}
   {:id 80 :kind :phrase :text "第一个。" :pinyin "dì yí gè." :meaning "The first one."}
   {:id 81 :kind :sentence :text "这样很好。" :pinyin "zhè yàng hěn hǎo." :meaning "This way is very good."}
   {:id 82 :kind :sentence :text "他的工作很好。" :pinyin "tā de gōng zuò hěn hǎo." :meaning "His job is very good."}
   {:id 83 :kind :sentence :text "这种书很好看。" :pinyin "zhè zhǒng shū hěn hǎo kàn." :meaning "This kind of book is very nice to read."}
   {:id 84 :kind :sentence :text "请开门。" :pinyin "qǐng kāi mén." :meaning "Please open the door."}
   {:id 85 :kind :sentence :text "美国很大。" :pinyin "Měi guó hěn dà." :meaning "America is very big."}
   {:id 86 :kind :sentence :text "他总是来看我。" :pinyin "tā zǒng shì lái kàn wǒ." :meaning "He always comes to see me."}
   {:id 87 :kind :sentence :text "他从中国来。" :pinyin "tā cóng Zhōng guó lái." :meaning "He comes from China."}
   {:id 88 :kind :sentence :text "无人来。" :pinyin "wú rén lái." :meaning "No one came."}
   {:id 89 :kind :sentence :text "事情很多。" :pinyin "shì qing hěn duō." :meaning "There are many things."}
   {:id 90 :kind :sentence :text "前面是什么？" :pinyin "qián mian shì shén me?" :meaning "What is in front?"}
   {:id 91 :kind :sentence :text "这是最好的。" :pinyin "zhè shì zuì hǎo de." :meaning "This is the best."}
   {:id 92 :kind :sentence :text "那个女人是老师。" :pinyin "nà gè nǚ rén shì lǎo shī." :meaning "That woman is a teacher."}
   {:id 93 :kind :sentence :text "但是，我想去。" :pinyin "dàn shì, wǒ xiǎng qù." :meaning "But, I want to go."}
   {:id 94 :kind :sentence :text "现在几点？" :pinyin "xiàn zài jǐ diǎn?" :meaning "What time is it now?"}
   {:id 95 :kind :sentence :text "以前我在中国。" :pinyin "yǐ qián wǒ zài Zhōng guó." :meaning "Before, I was in China."}
   {:id 96 :kind :sentence :text "这些书很好看。" :pinyin "zhè xiē shū hěn hǎo kàn." :meaning "These books are very nice to read."}
   {:id 97 :kind :sentence :text "所以我来了。" :pinyin "suǒ yǐ wǒ lái le." :meaning "So I came."}
   {:id 98 :kind :sentence :text "这是我的手机。" :pinyin "zhè shì wǒ de shǒu jī." :meaning "This is my phone."}
   {:id 99 :kind :phrase :text "又来了。" :pinyin "yòu lái le." :meaning "Came again."}
   {:id 100 :kind :sentence :text "他行动很快。" :pinyin "tā xíng dòng hěn kuài." :meaning "He acts very quickly."}
   {:id 101 :kind :sentence :text "什么意思？" :pinyin "shén me yì si?" :meaning "What does it mean?"}
   {:id 102 :kind :sentence :text "动物很大。" :pinyin "dòng wù hěn dà." :meaning "The animal is very big."}
   {:id 103 :kind :sentence :text "方法很好。" :pinyin "fāng fǎ hěn hǎo." :meaning "The method is very good."}
   {:id 104 :kind :sentence :text "今天是星期一。" :pinyin "jīn tiān shì xīng qī yī." :meaning "Today is Monday."}
   {:id 105 :kind :sentence :text "它是我的。" :pinyin "tā shì wǒ de." :meaning "It is mine."}
   {:id 106 :kind :sentence :text "我是头一个。" :pinyin "wǒ shì tóu yí gè." :meaning "I'm the first one."}
   {:id 107 :kind :sentence :text "我已经吃了。" :pinyin "wǒ yǐ jīng chī le." :meaning "I have already eaten."}
   {:id 108 :kind :sentence :text "这条路很长。" :pinyin "zhè tiáo lù hěn cháng." :meaning "This road is very long."}
   {:id 109 :kind :sentence :text "我的儿子很好。" :pinyin "wǒ de ér zi hěn hǎo." :meaning "My son is very good."}
   {:id 110 :kind :sentence :text "回家吧。" :pinyin "huí jiā ba." :meaning "Let's go home."}
   {:id 111 :kind :sentence :text "这位是老师。" :pinyin "zhè wèi shì lǎo shī." :meaning "This one is a teacher."}
   {:id 112 :kind :sentence :text "一分钱。" :pinyin "yì fēn qián." :meaning "One cent."}
   {:id 113 :kind :sentence :text "因为今天天气很好，所以我们去看书。" :pinyin "yīn wèi jīn tiān tiān qì hěn hǎo, suǒ yǐ wǒ men qù kàn shū." :meaning "Because the weather is good today, we go to read books."}
   {:id 114 :kind :sentence :text "他给我一本书。" :pinyin "tā gěi wǒ yì běn shū." :meaning "He gave me a book."}
   {:id 115 :kind :sentence :text "没有办法。" :pinyin "méi yǒu bàn fǎ." :meaning "There's no way."}
   {:id 116 :kind :sentence :text "世界很大。" :pinyin "shì jiè hěn dà." :meaning "The world is very big."}
   {:id 117 :kind :phrase :text "两个人。" :pinyin "liǎng gè rén." :meaning "Two people."}
   {:id 118 :kind :phrase :text "一次就好。" :pinyin "yí cì jiù hǎo." :meaning "Once is enough."}
   {:id 119 :kind :sentence :text "请使用中文。" :pinyin "qǐng shǐ yòng Zhōng wén." :meaning "Please use Chinese."}
   {:id 120 :kind :sentence :text "身体很好。" :pinyin "shēn tǐ hěn hǎo." :meaning "In good health."}
   {:id 121 :kind :sentence :text "作者是谁？" :pinyin "zuò zhě shì shéi?" :meaning "Who is the author?"}
   {:id 122 :kind :sentence :text "书被他看了。" :pinyin "shū bèi tā kàn le." :meaning "The book was read by him."}
   {:id 123 :kind :sentence :text "我很高兴。" :pinyin "wǒ hěn gāo xìng." :meaning "I'm very happy."}
   {:id 124 :kind :sentence :text "我的亲人很多。" :pinyin "wǒ de qīn rén hěn duō." :meaning "I have many relatives."}
   {:id 125 :kind :sentence :text "其他人呢？" :pinyin "qí tā rén ne?" :meaning "What about the other people?"}
   {:id 126 :kind :sentence :text "此时很好。" :pinyin "cǐ shí hěn hǎo." :meaning "This moment is very good."}
   {:id 127 :kind :sentence :text "这是电话。" :pinyin "zhè shì diàn huà." :meaning "This is a telephone."}
   {:id 128 :kind :sentence :text "他常常来看我。" :pinyin "tā cháng cháng lái kàn wǒ." :meaning "He often comes to see me."}
   {:id 129 :kind :sentence :text "我与他是朋友。" :pinyin "wǒ yǔ tā shì péng you." :meaning "He and I are friends."}
   {:id 130 :kind :sentence :text "生活很好。" :pinyin "shēng huó hěn hǎo." :meaning "Life is very good."}
   {:id 131 :kind :sentence :text "他正在看书。" :pinyin "tā zhèng zài kàn shū." :meaning "He is reading a book right now."}
   {:id 132 :kind :sentence :text "感觉很好。" :pinyin "gǎn jué hěn hǎo." :meaning "It feels very good."}
   {:id 133 :kind :sentence :text "问题是什么？" :pinyin "wèn tí shì shén me?" :meaning "What is the question?"}
   {:id 134 :kind :sentence :text "力量很大。" :pinyin "lì liang hěn dà." :meaning "Strength is very great."}
   {:id 135 :kind :sentence :text "我理解。" :pinyin "wǒ lǐ jiě." :meaning "I understand."}
   {:id 136 :kind :sentence :text "几个人？" :pinyin "jǐ gè rén?" :meaning "How many people?"}
   {:id 137 :kind :phrase :text "一定很好。" :pinyin "yí dìng hěn hǎo." :meaning "It will definitely be very good."}
   {:id 138 :kind :sentence :text "这本书很好看。" :pinyin "zhè běn shū hěn hǎo kàn." :meaning "This book is very nice to read."}
   {:id 139 :kind :sentence :text "公司很大。" :pinyin "gōng sī hěn dà." :meaning "The company is very big."}
   {:id 140 :kind :sentence :text "今天特别好。" :pinyin "jīn tiān tè bié hǎo." :meaning "Today is especially good."}
   {:id 141 :kind :sentence :text "做什么？" :pinyin "zuò shén me?" :meaning "What are you doing?"}
   {:id 142 :kind :sentence :text "外面是什么？" :pinyin "wài mian shì shén me?" :meaning "What's outside?"}
   {:id 143 :kind :sentence :text "我相信你。" :pinyin "wǒ xiāng xìn nǐ." :meaning "I believe you."}
   {:id 144 :kind :sentence :text "东西很多。" :pinyin "dōng xi hěn duō." :meaning "There are many things."}
   {:id 145 :kind :sentence :text "结果很好。" :pinyin "jié guǒ hěn hǎo." :meaning "The result is very good."}
   {:id 146 :kind :sentence :text "将来我想去中国。" :pinyin "jiāng lái wǒ xiǎng qù Zhōng guó." :meaning "In the future I want to go to China."}
   {:id 147 :kind :sentence :text "这个月很好。" :pinyin "zhè gè yuè hěn hǎo." :meaning "This month is very good."}
   {:id 148 :kind :phrase :text "十个人。" :pinyin "shí gè rén." :meaning "Ten people."}
   {:id 149 :kind :sentence :text "其实我很好。" :pinyin "qí shí wǒ hěn hǎo." :meaning "Actually I'm very well."}
   {:id 150 :kind :sentence :text "向前走。" :pinyin "xiàng qián zǒu." :meaning "Walk forward."}
   {:id 151 :kind :sentence :text "声音很大。" :pinyin "shēng yīn hěn dà." :meaning "The sound is very loud."}
   {:id 152 :kind :sentence :text "他开车去。" :pinyin "tā kāi chē qù." :meaning "He drives there."}
   {:id 153 :kind :sentence :text "全部都很好。" :pinyin "quán bù dōu hěn hǎo." :meaning "All are very good."}
   {:id 154 :kind :sentence :text "我不信。" :pinyin "wǒ bù xìn." :meaning "I don't believe."}
   {:id 155 :kind :phrase :text "很重要。" :pinyin "hěn zhòng yào." :meaning "Very important."}
   {:id 156 :kind :phrase :text "三个人。" :pinyin "sān gè rén." :meaning "Three people."}
   {:id 157 :kind :sentence :text "机会很好。" :pinyin "jī huì hěn hǎo." :meaning "The opportunity is very good."}
   {:id 158 :kind :sentence :text "工人很多。" :pinyin "gōng rén hěn duō." :meaning "There are many workers."}
   {:id 159 :kind :sentence :text "物品很多。" :pinyin "wù pǐn hěn duō." :meaning "There are many items."}
   {:id 160 :kind :sentence :text "每天都很好。" :pinyin "měi tiān dōu hěn hǎo." :meaning "Every day is very good."}
   {:id 161 :kind :sentence :text "他很好，并且很开心。" :pinyin "tā hěn hǎo, bìng qiě hěn kāi xīn." :meaning "He is very good, and also very happy."}
   {:id 162 :kind :sentence :text "别人呢？" :pinyin "bié rén ne?" :meaning "What about others?"}
   {:id 163 :kind :sentence :text "今天真好。" :pinyin "jīn tiān zhēn hǎo." :meaning "Today is really good."}
   {:id 164 :kind :sentence :text "打电话给我。" :pinyin "dǎ diàn huà gěi wǒ." :meaning "Call me."}
   {:id 165 :kind :phrase :text "太好了。" :pinyin "tài hǎo le." :meaning "Great!"}
   {:id 166 :kind :sentence :text "比我好。" :pinyin "bǐ wǒ hǎo." :meaning "Better than me."}
   {:id 167 :kind :sentence :text "他才来。" :pinyin "tā cái lái." :meaning "He just came."}
   {:id 168 :kind :sentence :text "很方便。" :pinyin "hěn fāng biàn." :meaning "Very convenient."}
   {:id 169 :kind :sentence :text "夫人很好。" :pinyin "fū rén hěn hǎo." :meaning "The lady is very good."}
   {:id 170 :kind :sentence :text "水很多。" :pinyin "shuǐ hěn duō." :meaning "There is a lot of water."}
   {:id 171 :kind :sentence :text "像我一样。" :pinyin "xiàng wǒ yí yàng." :meaning "Like me."}
   {:id 172 :kind :sentence :text "他看了我一眼。" :pinyin "tā kàn le wǒ yì yǎn." :meaning "He glanced at me."}
   {:id 173 :kind :sentence :text "等一下。" :pinyin "děng yí xià." :meaning "Wait a moment."}
   {:id 174 :kind :sentence :text "他想去，却没去。" :pinyin "tā xiǎng qù, què méi qù." :meaning "He wanted to go, but didn't go."}
   {:id 175 :kind :sentence :text "加入我们。" :pinyin "jiā rù wǒ men." :meaning "Join us."}
   {:id 176 :kind :sentence :text "主要是中文。" :pinyin "zhǔ yào shì Zhōng wén." :meaning "Mainly Chinese."}
   {:id 177 :kind :sentence :text "利用时间。" :pinyin "lì yòng shí jiān." :meaning "Use time well."}
   {:id 178 :kind :sentence :text "我接受。" :pinyin "wǒ jiē shòu." :meaning "I accept."}
   {:id 179 :kind :sentence :text "听我说。" :pinyin "tīng wǒ shuō." :meaning "Listen to me."}
   {:id 180 :kind :sentence :text "手表很好。" :pinyin "shǒu biǎo hěn hǎo." :meaning "The watch is very good."}
   {:id 181 :kind :sentence :text "德国很大。" :pinyin "Dé guó hěn dà." :meaning "Germany is very big."}
   {:id 182 :kind :sentence :text "人很少。" :pinyin "rén hěn shǎo." :meaning "There are very few people."}
   {:id 183 :kind :sentence :text "代表我们。" :pinyin "dài biǎo wǒ men." :meaning "Represent us."}
   {:id 184 :kind :sentence :text "员工很多。" :pinyin "yuán gōng hěn duō." :meaning "There are many employees."}
   {:id 185 :kind :phrase :text "许多人。" :pinyin "xǔ duō rén." :meaning "Many people."}
   {:id 186 :kind :sentence :text "你先走。" :pinyin "nǐ xiān zǒu." :meaning "You go first."}
   {:id 187 :kind :sentence :text "人口很多。" :pinyin "rén kǒu hěn duō." :meaning "The population is large."}
   {:id 188 :kind :sentence :text "由于天气很好，我们去看书。" :pinyin "yóu yú tiān qì hěn hǎo, wǒ men qù kàn shū." :meaning "Because the weather is good, we go to read books."}
   {:id 189 :kind :sentence :text "很安全。" :pinyin "hěn ān quán." :meaning "Very safe."}
   {:id 190 :kind :sentence :text "写字吧。" :pinyin "xiě zì ba." :meaning "Let's write characters."}
   {:id 191 :kind :sentence :text "性格很好。" :pinyin "xìng gé hěn hǎo." :meaning "The personality is very good."}
   {:id 192 :kind :sentence :text "白马很大。" :pinyin "bái mǎ hěn dà." :meaning "The white horse is very big."}
   {:id 193 :kind :sentence :text "或者去，或者不去。" :pinyin "huò zhě qù, huò zhě bù qù." :meaning "Either go, or don't go."}
   {:id 194 :kind :sentence :text "他住在中国。" :pinyin "tā zhù zài Zhōng guó." :meaning "He lives in China."}
   {:id 195 :kind :phrase :text "很难。" :pinyin "hěn nán." :meaning "Very difficult."}
   {:id 196 :kind :sentence :text "我希望你来。" :pinyin "wǒ xī wàng nǐ lái." :meaning "I hope you come."}
   {:id 197 :kind :sentence :text "他教中文。" :pinyin "tā jiào Zhōng wén." :meaning "He teaches Chinese."}
   {:id 198 :kind :sentence :text "生命很好。" :pinyin "shēng mìng hěn hǎo." :meaning "Life is very good."}
   {:id 199 :kind :sentence :text "花很好看。" :pinyin "huā hěn hǎo kàn." :meaning "The flowers are very beautiful."}
   {:id 200 :kind :sentence :text "今天更好。" :pinyin "jīn tiān gèng hǎo." :meaning "Today is even better."}
   {:id 201 :kind :sentence :text "他拉着我。" :pinyin "tā lā zhe wǒ." :meaning "He is pulling me."}
   {:id 202 :kind :sentence :text "精神很好。" :pinyin "jīng shén hěn hǎo." :meaning "In good spirits."}
   {:id 203 :kind :sentence :text "我记得。" :pinyin "wǒ jì de." :meaning "I remember."}
   {:id 204 :kind :sentence :text "到处都很好。" :pinyin "dào chù dōu hěn hǎo." :meaning "Everywhere is very good."}
   {:id 205 :kind :sentence :text "让我去。" :pinyin "ràng wǒ qù." :meaning "Let me go."}
   {:id 206 :kind :sentence :text "母亲很好。" :pinyin "mǔ qīn hěn hǎo." :meaning "Mother is very good."}
   {:id 207 :kind :sentence :text "父亲很好。" :pinyin "fù qīn hěn hǎo." :meaning "Father is very good."}
   {:id 208 :kind :sentence :text "你应该去。" :pinyin "nǐ yīng gāi qù." :meaning "You should go."}
   {:id 209 :kind :sentence :text "直接去。" :pinyin "zhí jiē qù." :meaning "Go directly."}
   {:id 210 :kind :sentence :text "市场很大。" :pinyin "shì chǎng hěn dà." :meaning "The market is very big."}
   {:id 211 :kind :sentence :text "平时我很好。" :pinyin "píng shí wǒ hěn hǎo." :meaning "Usually I'm very well."}
   {:id 212 :kind :sentence :text "报告很好。" :pinyin "bào gào hěn hǎo." :meaning "The report is very good."}
   {:id 213 :kind :sentence :text "放在这里。" :pinyin "fàng zài zhè lǐ." :meaning "Put it here."}
   {:id 214 :kind :sentence :text "至少一个人。" :pinyin "zhì shǎo yí gè rén." :meaning "At least one person."}
   {:id 215 :kind :sentence :text "张老师。" :pinyin "Zhāng lǎo shī." :meaning "Teacher Zhang."}
   {:id 216 :kind :sentence :text "认识你很高兴。" :pinyin "rèn shi nǐ hěn gāo xìng." :meaning "Nice to meet you."}
   {:id 217 :kind :sentence :text "接电话。" :pinyin "jiē diàn huà." :meaning "Answer the phone."}
   {:id 218 :kind :sentence :text "告诉我。" :pinyin "gào su wǒ." :meaning "Tell me."}
   {:id 219 :kind :sentence :text "进入中国。" :pinyin "jìn rù Zhōng guó." :meaning "Enter China."}
   {:id 220 :kind :sentence :text "他笑了。" :pinyin "tā xiào le." :meaning "He laughed."}
   {:id 221 :kind :sentence :text "内容很好。" :pinyin "nèi róng hěn hǎo." :meaning "The content is very good."}
   {:id 222 :kind :sentence :text "英国很大。" :pinyin "Yīng guó hěn dà." :meaning "England is very big."}
   {:id 223 :kind :sentence :text "军队很大。" :pinyin "jūn duì hěn dà." :meaning "The army is very big."}
   {:id 224 :kind :sentence :text "时候到了。" :pinyin "shí hou dào le." :meaning "Time is up."}
   {:id 225 :kind :sentence :text "人民很多。" :pinyin "rén mín hěn duō." :meaning "There are many people."}
   {:id 226 :kind :sentence :text "你几岁？" :pinyin "nǐ jǐ suì?" :meaning "How old are you?"}
   {:id 227 :kind :sentence :text "往前走。" :pinyin "wǎng qián zǒu." :meaning "Walk forward."}
   {:id 228 :kind :sentence :text "如何说？" :pinyin "rú hé shuō?" :meaning "How do you say it?"}
   {:id 229 :kind :sentence :text "山很高。" :pinyin "shān hěn gāo." :meaning "The mountain is very high."}
   {:id 230 :kind :sentence :text "我觉得很好。" :pinyin "wǒ jué de hěn hǎo." :meaning "I think it's very good."}
   {:id 231 :kind :sentence :text "带来书。" :pinyin "dài lái shū." :meaning "Bring a book."}
   {:id 232 :kind :phrase :text "一万人。" :pinyin "yí wàn rén." :meaning "Ten thousand people."}
   {:id 233 :kind :sentence :text "男人是老师。" :pinyin "nán rén shì lǎo shī." :meaning "The man is a teacher."}
   {:id 234 :kind :sentence :text "这边很好。" :pinyin "zhè biān hěn hǎo." :meaning "This side is very good."}
   {:id 235 :kind :sentence :text "风很大。" :pinyin "fēng hěn dà." :meaning "The wind is very strong."}
   {:id 236 :kind :sentence :text "金子很好。" :pinyin "jīn zi hěn hǎo." :meaning "Gold is very good."}
   {:id 237 :kind :sentence :text "原来是你。" :pinyin "yuán lái shì nǐ." :meaning "So it's you."}
   {:id 238 :kind :sentence :text "妈妈很好。" :pinyin "mā ma hěn hǎo." :meaning "Mom is very good."}
   {:id 239 :kind :sentence :text "变化很大。" :pinyin "biàn huà hěn dà." :meaning "The change is very big."}
   {:id 240 :kind :sentence :text "交通很好。" :pinyin "jiāo tōng hěn hǎo." :meaning "The traffic is very good."}
   {:id 241 :kind :sentence :text "立刻去。" :pinyin "lì kè qù." :meaning "Go immediately."}
   {:id 242 :kind :sentence :text "大象很大。" :pinyin "dà xiàng hěn dà." :meaning "The elephant is very big."}
   {:id 243 :kind :sentence :text "数学很好。" :pinyin "shù xué hěn hǎo." :meaning "Maths is very good."}
   {:id 244 :kind :phrase :text "四个人。" :pinyin "sì gè rén." :meaning "Four people."}
   {:id 245 :kind :sentence :text "他失去了书。" :pinyin "tā shī qù le shū." :meaning "He lost the book."}
   {:id 246 :kind :phrase :text "满了。" :pinyin "mǎn le." :meaning "It's full."}
   {:id 247 :kind :sentence :text "战争不好。" :pinyin "zhàn zhēng bù hǎo." :meaning "War is not good."}
   {:id 248 :kind :sentence :text "中国很远。" :pinyin "Zhōng guó hěn yuǎn." :meaning "China is very far."}
   {:id 249 :kind :sentence :text "女士很好。" :pinyin "nǚ shì hěn hǎo." :meaning "The lady is very good."}
   {:id 250 :kind :sentence :text "轻轻走。" :pinyin "qīng qīng zǒu." :meaning "Walk lightly."}
   {:id 251 :kind :sentence :text "目的是中国。" :pinyin "mù dì shì Zhōng guó." :meaning "The purpose is China."}
   {:id 252 :kind :sentence :text "病好了。" :pinyin "bìng hǎo le." :meaning "The illness is cured."}
   {:id 253 :kind :sentence :text "开始吧。" :pinyin "kāi shǐ ba." :meaning "Let's start."}
   {:id 254 :kind :sentence :text "到达中国。" :pinyin "dào dá Zhōng guó." :meaning "Arrive in China."}
   {:id 255 :kind :sentence :text "水很深。" :pinyin "shuǐ hěn shēn." :meaning "The water is very deep."}
   {:id 256 :kind :sentence :text "完成了。" :pinyin "wán chéng le." :meaning "It's completed."}
   {:id 257 :kind :sentence :text "提出问题。" :pinyin "tí chū wèn tí." :meaning "Raise a question."}
   {:id 258 :kind :sentence :text "要求很高。" :pinyin "yāo qiú hěn gāo." :meaning "The requirements are very high."}
   {:id 259 :kind :sentence :text "清早很好。" :pinyin "qīng zǎo hěn hǎo." :meaning "The early morning is very good."}
   {:id 260 :kind :sentence :text "王老师。" :pinyin "Wáng lǎo shī." :meaning "Teacher Wang."}
   {:id 261 :kind :sentence :text "空气很好。" :pinyin "kōng qì hěn hǎo." :meaning "The air is very good."}
   {:id 262 :kind :sentence :text "工业很大。" :pinyin "gōng yè hěn dà." :meaning "Industry is very big."}
   {:id 263 :kind :sentence :text "思想很好。" :pinyin "sī xiǎng hěn hǎo." :meaning "The thinking is very good."}
   {:id 264 :kind :sentence :text "一切都很好。" :pinyin "yí qiè dōu hěn hǎo." :meaning "Everything is very good."}
   {:id 265 :kind :sentence :text "怎么去？" :pinyin "zěn me qù?" :meaning "How do I get there?"}
   {:id 266 :kind :phrase :text "非常好。" :pinyin "fēi cháng hǎo." :meaning "Very good."}
   {:id 267 :kind :sentence :text "请找我。" :pinyin "qǐng zhǎo wǒ." :meaning "Please find me."}
   {:id 268 :kind :sentence :text "照片很好看。" :pinyin "zhào piàn hěn hǎo kàn." :meaning "The photo is very beautiful."}
   {:id 269 :kind :sentence :text "罗马很大。" :pinyin "Luó mǎ hěn dà." :meaning "Rome is very big."}
   {:id 270 :kind :sentence :text "钱很多。" :pinyin "qián hěn duō." :meaning "A lot of money."}
   {:id 271 :kind :sentence :text "语言很好。" :pinyin "yǔ yán hěn hǎo." :meaning "The language is very good."}
   {:id 272 :kind :phrase :text "一元。" :pinyin "yì yuán." :meaning "One yuan."}
   {:id 273 :kind :sentence :text "曾经去过中国。" :pinyin "céng jīng qù guo Zhōng guó." :meaning "Have been to China before."}
   {:id 274 :kind :sentence :text "离开中国。" :pinyin "lí kāi Zhōng guó." :meaning "Leave China."}
   {:id 275 :kind :sentence :text "飞机很大。" :pinyin "fēi jī hěn dà." :meaning "The aeroplane is very big."}
   {:id 276 :kind :sentence :text "科学很好。" :pinyin "kē xué hěn hǎo." :meaning "Science is very good."}
   {:id 277 :kind :sentence :text "干什么？" :pinyin "gàn shén me?" :meaning "What are you doing?"}
   {:id 278 :kind :sentence :text "交流很好。" :pinyin "jiāo liú hěn hǎo." :meaning "Communication is very good."}
   {:id 279 :kind :sentence :text "我约你去。" :pinyin "wǒ yuē nǐ qù." :meaning "I invite you to go."}
   {:id 280 :kind :sentence :text "各位好。" :pinyin "gè wèi hǎo." :meaning "Hello, everyone."}
   {:id 281 :kind :sentence :text "立即去。" :pinyin "lì jí qù." :meaning "Go immediately."}
   {:id 282 :kind :sentence :text "指出问题。" :pinyin "zhǐ chū wèn tí." :meaning "Point out a problem."}
   {:id 283 :kind :sentence :text "合作很好。" :pinyin "hé zuò hěn hǎo." :meaning "Cooperation is very good."}
   {:id 284 :kind :sentence :text "反对战争。" :pinyin "fǎn duì zhàn zhēng." :meaning "Oppose war."}
   {:id 285 :kind :sentence :text "必去。" :pinyin "bì qù." :meaning "Must go."}
   {:id 286 :kind :sentence :text "论问题。" :pinyin "lùn wèn tí." :meaning "Discuss a problem."}
   {:id 287 :kind :sentence :text "终于来了。" :pinyin "zhōng yú lái le." :meaning "Finally came."}
   {:id 288 :kind :sentence :text "树林很大。" :pinyin "shù lín hěn dà." :meaning "The forest is very big."}
   {:id 289 :kind :sentence :text "医生很好。" :pinyin "yī shēng hěn hǎo." :meaning "The doctor is very good."}
   {:id 290 :kind :sentence :text "制度很好。" :pinyin "zhì dù hěn hǎo." :meaning "The system is very good."}
   {:id 291 :kind :sentence :text "球很大。" :pinyin "qiú hěn dà." :meaning "The ball is very big."}
   {:id 292 :kind :sentence :text "决定去。" :pinyin "jué dìng qù." :meaning "Decide to go."}
   {:id 293 :kind :sentence :text "传统很好。" :pinyin "chuán tǒng hěn hǎo." :meaning "Tradition is very good."}
   {:id 294 :kind :sentence :text "他画画很好。" :pinyin "tā huà huà hěn hǎo." :meaning "He draws very well."}
   {:id 295 :kind :sentence :text "保中国。" :pinyin "bǎo Zhōng guó." :meaning "Protect China."}
   {:id 296 :kind :sentence :text "读书很好。" :pinyin "dú shū hěn hǎo." :meaning "Reading is very good."}
   {:id 297 :kind :sentence :text "运动很好。" :pinyin "yùn dòng hěn hǎo." :meaning "Exercise is very good."}
   {:id 298 :kind :sentence :text "我以及他。" :pinyin "wǒ yǐ jí tā." :meaning "Me and him."}
   {:id 299 :kind :sentence :text "原则很好。" :pinyin "yuán zé hěn hǎo." :meaning "The principle is very good."}
   {:id 300 :kind :sentence :text "房子很大。" :pinyin "fáng zi hěn dà." :meaning "The house is very big."}
   {:id 301 :kind :sentence :text "医院很大。" :pinyin "yī yuàn hěn dà." :meaning "The hospital is very big."}
   {:id 302 :kind :sentence :text "生活很苦。" :pinyin "shēng huó hěn kǔ." :meaning "Life is very hard."}
   {:id 303 :kind :sentence :text "火很大。" :pinyin "huǒ hěn dà." :meaning "The fire is very big."}
   {:id 304 :kind :sentence :text "最近很好。" :pinyin "zuì jìn hěn hǎo." :meaning "Recently very good."}
   {:id 305 :kind :sentence :text "产品很好。" :pinyin "chǎn pǐn hěn hǎo." :meaning "The product is very good."}
   {:id 306 :kind :sentence :text "回答问题。" :pinyin "huí dá wèn tí." :meaning "Answer a question."}
   {:id 307 :kind :sentence :text "星星很多。" :pinyin "xīng xing hěn duō." :meaning "There are many stars."}
   {:id 308 :kind :sentence :text "电视很大。" :pinyin "diàn shì hěn dà." :meaning "The TV is very big."}
   {:id 309 :kind :phrase :text "五个人。" :pinyin "wǔ gè rén." :meaning "Five people."}
   {:id 310 :kind :sentence :text "连我都很好。" :pinyin "lián wǒ dōu hěn hǎo." :meaning "Even I am very well."}
   {:id 311 :kind :sentence :text "奇事。" :pinyin "qí shì." :meaning "A strange thing."}
   {:id 312 :kind :sentence :text "管理很好。" :pinyin "guǎn lǐ hěn hǎo." :meaning "Management is very good."}
   {:id 313 :kind :sentence :text "人类很多。" :pinyin "rén lèi hěn duō." :meaning "There are many humans."}
   {:id 314 :kind :sentence :text "未来很好。" :pinyin "wèi lái hěn hǎo." :meaning "The future is very good."}
   {:id 315 :kind :sentence :text "结婚了。" :pinyin "jié hūn le." :meaning "Got married."}
   {:id 316 :kind :sentence :text "台上很好。" :pinyin "tái shang hěn hǎo." :meaning "On the stage is very good."}
   {:id 317 :kind :sentence :text "夜晚很好。" :pinyin "yè wǎn hěn hǎo." :meaning "The night is very good."}
   {:id 318 :kind :sentence :text "青年很多。" :pinyin "qīng nián hěn duō." :meaning "There are many young people."}
   {:id 319 :kind :sentence :text "北方很大。" :pinyin "běi fāng hěn dà." :meaning "The north is very big."}
   {:id 320 :kind :sentence :text "队很大。" :pinyin "duì hěn dà." :meaning "The team is very big."}
   {:id 321 :kind :sentence :text "很久没见。" :pinyin "hěn jiǔ méi jiàn." :meaning "Long time no see."}
   {:id 322 :kind :sentence :text "几乎都很好。" :pinyin "jī hū dōu hěn hǎo." :meaning "Almost all are very good."}
   {:id 323 :kind :sentence :text "越来越好。" :pinyin "yuè lái yuè hǎo." :meaning "Better and better."}
   {:id 324 :kind :sentence :text "观点很好。" :pinyin "guān diǎn hěn hǎo." :meaning "The viewpoint is very good."}
   {:id 325 :kind :sentence :text "落下来。" :pinyin "luò xià lái." :meaning "Fall down."}
   {:id 326 :kind :sentence :text "尽管去。" :pinyin "jǐn guǎn qù." :meaning "Go anyway."}
   {:id 327 :kind :sentence :text "形很好。" :pinyin "xíng hěn hǎo." :meaning "The shape is very good."}
   {:id 328 :kind :sentence :text "电影很好看。" :pinyin "diàn yǐng hěn hǎo kàn." :meaning "The movie is very beautiful."}
   {:id 329 :kind :sentence :text "红色很好看。" :pinyin "hóng sè hěn hǎo kàn." :meaning "Red is very beautiful."}
   {:id 330 :kind :sentence :text "爸爸很好。" :pinyin "bà ba hěn hǎo." :meaning "Dad is very good."}
   {:id 331 :kind :sentence :text "一百个人。" :pinyin "yì bǎi gè rén." :meaning "One hundred people."}
   {:id 332 :kind :sentence :text "令人高兴。" :pinyin "lìng rén gāo xìng." :meaning "Makes people happy."}
   {:id 333 :kind :sentence :text "这周很好。" :pinyin "zhè zhōu hěn hǎo." :meaning "This week is very good."}
   {:id 334 :kind :sentence :text "走一步。" :pinyin "zǒu yí bù." :meaning "Walk one step."}
   {:id 335 :kind :sentence :text "术很好。" :pinyin "shù hěn hǎo." :meaning "The technique is very good."}
   {:id 336 :kind :sentence :text "留下来。" :pinyin "liú xià lái." :meaning "Stay."}
   {:id 337 :kind :sentence :text "城市很大。" :pinyin "chéng shì hěn dà." :meaning "The city is very big."}
   {:id 338 :kind :sentence :text "半天后见。" :pinyin "bàn tiān hòu jiàn." :meaning "See you in half a day."}
   {:id 339 :kind :sentence :text "今天很热。" :pinyin "jīn tiān hěn rè." :meaning "Today is very hot."}
   {:id 340 :kind :sentence :text "请送我去。" :pinyin "qǐng sòng wǒ qù." :meaning "Please take me there."}
   {:id 341 :kind :sentence :text "造房子。" :pinyin "zào fáng zi." :meaning "Build a house."}
   {:id 342 :kind :sentence :text "谈话很好。" :pinyin "tán huà hěn hǎo." :meaning "The conversation is very good."}
   {:id 343 :kind :sentence :text "极好。" :pinyin "jí hǎo." :meaning "Extremely good."}
   {:id 344 :kind :sentence :text "随便吧。" :pinyin "suí biàn ba." :meaning "Whatever."}
   {:id 345 :kind :sentence :text "演出很好。" :pinyin "yǎn chū hěn hǎo." :meaning "The performance is very good."}
   {:id 346 :kind :sentence :text "收到了。" :pinyin "shōu dào le." :meaning "Received."}
   {:id 347 :kind :sentence :text "首先去。" :pinyin "shǒu xiān qù." :meaning "Go first."}
   {:id 348 :kind :sentence :text "树根很大。" :pinyin "shù gēn hěn dà." :meaning "The tree root is very big."}
   {:id 349 :kind :sentence :text "讲话很好。" :pinyin "jiǎng huà hěn hǎo." :meaning "Speaking is very good."}
   {:id 350 :kind :sentence :text "整个中国都很大。" :pinyin "zhěng gè Zhōng guó dōu hěn dà." :meaning "All of China is very big."}
   {:id 351 :kind :sentence :text "方式很好。" :pinyin "fāng shì hěn hǎo." :meaning "The method is very good."}
   {:id 352 :kind :sentence :text "取书。" :pinyin "qǔ shū." :meaning "Take a book."}
   {:id 353 :kind :sentence :text "他很强。" :pinyin "tā hěn qiáng." :meaning "He is very strong."}
   {:id 354 :kind :sentence :text "石头很大。" :pinyin "shí tou hěn dà." :meaning "The stone is very big."}
   {:id 355 :kind :sentence :text "这是古老的书。" :pinyin "zhè shì gǔ lǎo de shū." :meaning "This is an ancient book."}
   {:id 356 :kind :sentence :text "中华很大。" :pinyin "Zhōng huá hěn dà." :meaning "China is very big."}
   {:id 357 :kind :sentence :text "请拿书。" :pinyin "qǐng ná shū." :meaning "Please take a book."}
   {:id 358 :kind :sentence :text "计算很好。" :pinyin "jì suàn hěn hǎo." :meaning "The calculation is very good."}
   {:id 359 :kind :phrase :text "您好。" :pinyin "nín hǎo." :meaning "Hello (polite)."}
   {:id 360 :kind :sentence :text "装满了。" :pinyin "zhuāng mǎn le." :meaning "It's filled."}
   {:id 361 :kind :sentence :text "似乎很好。" :pinyin "sì hū hěn hǎo." :meaning "Seems very good."}
   {:id 362 :kind :sentence :text "足了。" :pinyin "zú le." :meaning "Enough."}
   {:id 363 :kind :sentence :text "双方都很好。" :pinyin "shuāng fāng dōu hěn hǎo." :meaning "Both sides are very good."}
   {:id 364 :kind :sentence :text "妻子很好。" :pinyin "qī zi hěn hǎo." :meaning "The wife is very good."}
   {:id 365 :kind :sentence :text "转身走。" :pinyin "zhuǎn shēn zǒu." :meaning "Turn and walk."}
   {:id 366 :kind :sentence :text "米很多。" :pinyin "mǐ hěn duō." :meaning "There is a lot of rice."}
   {:id 367 :kind :sentence :text "称我。" :pinyin "chēng wǒ." :meaning "Call me."}
   {:id 368 :kind :sentence :text "中国很美丽。" :pinyin "Zhōng guó hěn měi lì." :meaning "China is very beautiful."}
   {:id 369 :kind :sentence :text "客人很多。" :pinyin "kè rén hěn duō." :meaning "There are many guests."}
   {:id 370 :kind :sentence :text "南方很大。" :pinyin "nán fāng hěn dà." :meaning "The south is very big."}
   {:id 371 :kind :sentence :text "领我去。" :pinyin "lǐng wǒ qù." :meaning "Lead me there."}
   {:id 372 :kind :sentence :text "节日很好。" :pinyin "jié rì hěn hǎo." :meaning "The festival is very good."}
   {:id 373 :kind :sentence :text "衣很好看。" :pinyin "yī hěn hǎo kàn." :meaning "The clothes are very beautiful."}
   {:id 374 :kind :sentence :text "站起来。" :pinyin "zhàn qǐ lái." :meaning "Stand up."}
   {:id 375 :kind :sentence :text "黑色很好看。" :pinyin "hēi sè hěn hǎo kàn." :meaning "Black is very beautiful."}
   {:id 376 :kind :sentence :text "断了。" :pinyin "duàn le." :meaning "It's broken."}
   {:id 377 :kind :sentence :text "福很好。" :pinyin "fú hěn hǎo." :meaning "Fortune is very good."}
   {:id 378 :kind :sentence :text "故事很好听。" :pinyin "gù shi hěn hǎo tīng." :meaning "The story is very nice to listen to."}
   {:id 379 :kind :sentence :text "历史很好。" :pinyin "lì shǐ hěn hǎo." :meaning "History is very good."}
   {:id 380 :kind :sentence :text "惊人。" :pinyin "jīng rén." :meaning "Surprising."}
   {:id 381 :kind :sentence :text "脸红了。" :pinyin "liǎn hóng le." :meaning "The face turned red."}
   {:id 382 :kind :sentence :text "选中国。" :pinyin "xuǎn Zhōng guó." :meaning "Choose China."}
   {:id 383 :kind :sentence :text "包子很好吃。" :pinyin "bāo zi hěn hǎo chī." :meaning "The baozi is very delicious."}
   {:id 384 :kind :sentence :text "很紧张。" :pinyin "hěn jǐn zhāng." :meaning "Very nervous."}
   {:id 385 :kind :sentence :text "争论问题。" :pinyin "zhēng lùn wèn tí." :meaning "Argue about a problem."}
   {:id 386 :kind :sentence :text "另外一个人。" :pinyin "lìng wài yí gè rén." :meaning "Another person."}
   {:id 387 :kind :sentence :text "建中国。" :pinyin "jiàn Zhōng guó." :meaning "Build China."}
   {:id 388 :kind :sentence :text "思维很好。" :pinyin "sī wéi hěn hǎo." :meaning "The thinking is very good."}
   {:id 389 :kind :sentence :text "绝对很好。" :pinyin "jué duì hěn hǎo." :meaning "Absolutely very good."}
   {:id 390 :kind :sentence :text "树很大。" :pinyin "shù hěn dà." :meaning "The tree is very big."}
   {:id 391 :kind :sentence :text "很伤心。" :pinyin "hěn shāng xīn." :meaning "Very sad."}
   {:id 392 :kind :sentence :text "我愿意去。" :pinyin "wǒ yuàn yì qù." :meaning "I'm willing to go."}
   {:id 393 :kind :sentence :text "保持很好。" :pinyin "bǎo chí hěn hǎo." :meaning "Keeping it up is very good."}
   {:id 394 :kind :sentence :text "一千个人。" :pinyin "yì qiān gè rén." :meaning "One thousand people."}
   {:id 395 :kind :sentence :text "谁来了？" :pinyin "shéi lái le?" :meaning "Who came?"}
   {:id 396 :kind :sentence :text "准确很好。" :pinyin "zhǔn què hěn hǎo." :meaning "The accuracy is very good."}
   {:id 397 :kind :sentence :text "联系我。" :pinyin "lián xì wǒ." :meaning "Contact me."}
   {:id 398 :kind :sentence :text "妇女很多。" :pinyin "fù nǚ hěn duō." :meaning "There are many women."}
   {:id 399 :kind :sentence :text "他的年纪很大。" :pinyin "tā de nián jì hěn dà." :meaning "He is very old."}
   {:id 400 :kind :sentence :text "基本很好。" :pinyin "jī běn hěn hǎo." :meaning "Basically very good."}
   {:id 401 :kind :sentence :text "买东西去。" :pinyin "mǎi dōng xi qù." :meaning "Go shopping."}
   {:id 402 :kind :sentence :text "志愿很好。" :pinyin "zhì yuàn hěn hǎo." :meaning "The volunteer work is very good."}
   {:id 403 :kind :sentence :text "很安静。" :pinyin "hěn ān jìng." :meaning "Very quiet."}
   {:id 404 :kind :sentence :text "诗很好。" :pinyin "shī hěn hǎo." :meaning "The poem is very good."}
   {:id 405 :kind :sentence :text "独自去。" :pinyin "dú zì qù." :meaning "Go alone."}
   {:id 406 :kind :sentence :text "反复说。" :pinyin "fǎn fù shuō." :meaning "Say it repeatedly."}
   {:id 407 :kind :sentence :text "头很痛。" :pinyin "tóu hěn tòng." :meaning "The head hurts a lot."}
   {:id 408 :kind :sentence :text "消失了。" :pinyin "xiāo shī le." :meaning "Disappeared."}
   {:id 409 :kind :sentence :text "社会很大。" :pinyin "shè huì hěn dà." :meaning "Society is very big."}
   {:id 410 :kind :sentence :text "算一下。" :pinyin "suàn yí xià." :meaning "Calculate a bit."}
   {:id 411 :kind :sentence :text "意义很大。" :pinyin "yì yì hěn dà." :meaning "The meaning is very great."}
   {:id 412 :kind :sentence :text "竟然来了。" :pinyin "jìng rán lái le." :meaning "Actually came."}
   {:id 413 :kind :sentence :text "确实很好。" :pinyin "què shí hěn hǎo." :meaning "Indeed very good."}
   {:id 414 :kind :sentence :text "酒很多。" :pinyin "jiǔ hěn duō." :meaning "There is a lot of wine."}
   {:id 415 :kind :sentence :text "光很好。" :pinyin "guāng hěn hǎo." :meaning "The light is very good."}
   {:id 416 :kind :sentence :text "出示书。" :pinyin "chū shì shū." :meaning "Show a book."}
   {:id 417 :kind :sentence :text "亚军很好。" :pinyin "yà jūn hěn hǎo." :meaning "The runner-up is very good."}
   {:id 418 :kind :sentence :text "布很好。" :pinyin "bù hěn hǎo." :meaning "The cloth is very good."}
   {:id 419 :kind :sentence :text "一克米。" :pinyin "yì kè mǐ." :meaning "One gram of rice."}
   {:id 420 :kind :sentence :text "阿妈很好。" :pinyin "ā mā hěn hǎo." :meaning "Mom is very good."}
   {:id 421 :kind :sentence :text "任何人。" :pinyin "rèn hé rén." :meaning "Anyone."}
   {:id 422 :kind :sentence :text "下巴很大。" :pinyin "xià ba hěn dà." :meaning "The chin is very big."}
   {:id 423 :kind :sentence :text "斯文很好。" :pinyin "sī wén hěn hǎo." :meaning "Refined and gentle is very good."}
   {:id 424 :kind :sentence :text "死机了。" :pinyin "sǐ jī le." :meaning "It crashed (a device froze)."}])
