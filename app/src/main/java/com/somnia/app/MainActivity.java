package com.somnia.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;
import org.json.*;

public class MainActivity extends Activity {
  static final int BG=Color.rgb(9,16,14), PANEL=Color.rgb(19,29,26), TEXT=Color.rgb(245,250,247), MUTED=Color.rgb(139,155,149), ACCENT=Color.rgb(59,221,174);
  LinearLayout root, content; android.content.SharedPreferences prefs; JSONArray entries; int page=0;
  int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
  public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);prefs=getSharedPreferences("somnia",MODE_PRIVATE);load();show(0);}
  void load(){try{entries=new JSONArray(prefs.getString("entries","[]"));}catch(Exception e){entries=new JSONArray();}}
  void save(){prefs.edit().putString("entries",entries.toString()).apply();}
  TextView text(String s,int size,int color){TextView v=new TextView(this);v.setText(s);v.setTextColor(color);v.setTextSize(size);v.setPadding(0,dp(7),0,dp(7));return v;}
  void add(String s,int size,int color){content.addView(text(s,size,color));}
  LinearLayout panel(){LinearLayout p=new LinearLayout(this);p.setOrientation(1);p.setPadding(dp(18),dp(14),dp(18),dp(14));android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();d.setColor(PANEL);d.setCornerRadius(dp(22));p.setBackground(d);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(10),0,dp(10));content.addView(p,lp);return p;}
  Button button(String s,LinearLayout parent,Runnable click){Button b=new Button(this);b.setText(s);b.setTextColor(TEXT);b.setAllCaps(false);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ACCENT));parent.addView(b,new LinearLayout.LayoutParams(-1,dp(52)));b.setOnClickListener(v->click.run());return b;}
  void show(int tab){page=tab;root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(BG);setContentView(root);TextView brand=text("☾  SOMNIA",23,TEXT);brand.setPadding(dp(24),dp(15),0,dp(10));root.addView(brand);ScrollView scroll=new ScrollView(this);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));content=new LinearLayout(this);content.setPadding(dp(22),0,dp(22),dp(20));content.setOrientation(1);scroll.addView(content);if(tab==0)home();if(tab==1)stats();if(tab==2)history();if(tab==3)settings();LinearLayout nav=new LinearLayout(this);nav.setPadding(dp(5),dp(5),dp(5),dp(5));root.addView(nav);String[] names={"Главная","Графики","История","Настройки"};for(int i=0;i<4;i++){final int t=i;TextView v=text(names[i],12,i==tab?ACCENT:MUTED);v.setGravity(Gravity.CENTER);nav.addView(v,new LinearLayout.LayoutParams(0,dp(52),1));v.setOnClickListener(w->show(t));}}
  long active(){return prefs.getLong("active",0);}
  void home(){add("Твой сон в точках",22,TEXT);add("Каждая точка — одна ночь",14,MUTED);content.addView(new YearCalendar(this,entries),new LinearLayout.LayoutParams(-1,dp(290)));add("Часы сна",15,MUTED);add("🔴 <6 ч      ● 6–7 ч      ● 7–8 ч      🟢 8–9 ч      🟠 9+ ч",12,MUTED);LinearLayout p=panel();p.addView(text(active()>0?"Сон записывается…":"Готов ко сну?",22,TEXT));p.addView(text(active()>0?"Начало: "+fmt(active()):"Нажми, когда ложишься спать",15,MUTED));button(active()>0?"☀ Завершить сон":"☾ Начать сон",p,()->{if(active()==0){prefs.edit().putLong("active",System.currentTimeMillis()).apply();}else{record(active(),System.currentTimeMillis());prefs.edit().remove("active").apply();}show(0);});if(entries.length()>0){JSONObject last=entries.optJSONObject(entries.length()-1);if(last!=null){long duration=last.optLong("end")-last.optLong("start");add("Последняя запись",18,TEXT);LinearLayout q=panel();q.addView(text(duration(duration),28,ACCENT));q.addView(text(fmt(last.optLong("start"))+" → "+fmt(last.optLong("end")),14,MUTED));}}add("Совет дня",19,TEXT);LinearLayout tip=panel();tip.addView(text("Старайся ложиться и просыпаться примерно в одно время. Время сна — лишь один из показателей отдыха.",15,TEXT));}
  void record(long start,long end){if(end<=start)return;JSONObject o=new JSONObject();try{o.put("start",start);o.put("end",end);entries.put(o);save();}catch(Exception ignored){}}
  String fmt(long t){return new SimpleDateFormat("dd.MM HH:mm",Locale.getDefault()).format(new Date(t));}
  String duration(long ms){long min=Math.max(0,ms/60000);return (min/60)+" ч "+(min%60)+" мин";}
  void stats(){add("Статистика сна",24,TEXT);int n=entries.length();if(n==0){add("Пока нет записей. Начни отслеживать сон на главной.",15,MUTED);return;}long sum=0;int valid=0;for(int i=0;i<n;i++){JSONObject o=entries.optJSONObject(i);if(o!=null){sum+=o.optLong("end")-o.optLong("start");valid++;}}LinearLayout p=panel();p.addView(text("Средняя продолжительность",16,MUTED));p.addView(text(duration(sum/Math.max(1,valid)),30,ACCENT));p.addView(text("Записей: "+valid,14,TEXT));content.addView(new Graph(this,entries),new LinearLayout.LayoutParams(-1,dp(230)));add("На графике показаны последние 7 записей. Данные основаны на ручном запуске и завершении сна.",13,MUTED);}
  void history(){add("История сна",24,TEXT);if(entries.length()==0){add("Здесь появятся завершённые сеансы сна.",15,MUTED);return;}for(int i=entries.length()-1;i>=0;i--){JSONObject o=entries.optJSONObject(i);if(o==null)continue;LinearLayout p=panel();p.addView(text(fmt(o.optLong("start"))+" → "+fmt(o.optLong("end")),15,TEXT));p.addView(text(duration(o.optLong("end")-o.optLong("start")),22,ACCENT));}button("Очистить историю",content,()->new AlertDialog.Builder(this).setTitle("Удалить все записи?").setNegativeButton("Отмена",null).setPositiveButton("Удалить",(d,w)->{entries=new JSONArray();save();show(2);}).show());}
  void settings(){add("Настройки",24,TEXT);add("Время отхода ко сну",17,TEXT);int h=prefs.getInt("hour",22),m=prefs.getInt("minute",30);LinearLayout p=panel();p.addView(text(String.format(Locale.getDefault(),"%02d:%02d",h,m),26,ACCENT));button("Изменить время",p,()->new TimePickerDialog(this,(v,hour,minute)->{prefs.edit().putInt("hour",hour).putInt("minute",minute).apply();scheduleReminder(this);show(3);},h,m,true).show());Switch reminder=new Switch(this);reminder.setText("Ежедневное напоминание");reminder.setTextColor(TEXT);reminder.setChecked(prefs.getBoolean("reminder",false));content.addView(reminder);reminder.setOnCheckedChangeListener((b,on)->{prefs.edit().putBoolean("reminder",on).apply();if(on && Build.VERSION.SDK_INT>=33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=android.content.pm.PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},11);scheduleReminder(this);});add("Цель сна (часов)",17,TEXT);SeekBar seek=new SeekBar(this);seek.setMax(6);seek.setProgress(prefs.getInt("goal",8)-6);content.addView(seek);TextView goal=text("Цель: "+prefs.getInt("goal",8)+" ч",16,ACCENT);content.addView(goal);seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int progress,boolean fromUser){int v=progress+6;goal.setText("Цель: "+v+" ч");prefs.edit().putInt("goal",v).apply();}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});add("Все данные сохраняются только на устройстве. Напоминание не является умным будильником, определяющим фазы сна.",13,MUTED);}
  static void scheduleReminder(Context c){android.content.SharedPreferences p=c.getSharedPreferences("somnia",MODE_PRIVATE);AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);Intent intent=new Intent(c,ReminderReceiver.class);PendingIntent pi=PendingIntent.getBroadcast(c,1,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);if(!p.getBoolean("reminder",false)){am.cancel(pi);return;}Calendar cal=Calendar.getInstance();cal.set(Calendar.HOUR_OF_DAY,p.getInt("hour",22));cal.set(Calendar.MINUTE,p.getInt("minute",30));cal.set(Calendar.SECOND,0);if(cal.getTimeInMillis()<=System.currentTimeMillis())cal.add(Calendar.DAY_OF_YEAR,1);am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),pi);}
  static class YearCalendar extends View {
    final JSONArray data; final Paint paint=new Paint(3); final int year; final HashMap<String,Float> hours=new HashMap<>();
    final int[] palette={Color.rgb(150,53,62),Color.rgb(37,104,87),Color.rgb(47,151,120),Color.rgb(59,221,174),Color.rgb(220,160,85)};
    YearCalendar(Context ctx,JSONArray records){super(ctx);data=records;year=Calendar.getInstance().get(Calendar.YEAR);setLayerType(View.LAYER_TYPE_SOFTWARE,null);
      SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd",Locale.US);
      for(int i=0;i<data.length();i++){JSONObject o=data.optJSONObject(i);if(o==null)continue;long end=o.optLong("end"),start=o.optLong("start");if(end>start){String key=df.format(new Date(end));hours.put(key,(end-start)/3600000f);}}
    }
    protected void onDraw(Canvas canvas){super.onDraw(canvas);float density=getResources().getDisplayMetrics().density;float width=getWidth(),height=getHeight();
      paint.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));paint.setTextSize(31*density);paint.setColor(TEXT);canvas.drawText(String.valueOf(year),0,37*density,paint);
      paint.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));paint.setTextSize(12*density);paint.setColor(MUTED);canvas.drawText(hours.size()+" ночей",width-85*density,32*density,paint);
      Calendar cal=new GregorianCalendar(year,0,1);int offset=(cal.get(Calendar.DAY_OF_WEEK)+5)%7;
      int weeks=(new GregorianCalendar(year,11,31).get(Calendar.DAY_OF_YEAR)+offset+6)/7;
      float left=31*density, top=91*density, gap=Math.min(10.5f*density,(width-left-4*density)/weeks),r=Math.max(2.2f*density,Math.min(3.4f*density,gap*.31f));
      String[] weekdays={"Пн","Ср","Пт","Вс"};paint.setTextSize(10*density);paint.setColor(MUTED);
      for(int i=0;i<4;i++)canvas.drawText(weekdays[i],0,top+(i*2)*gap+3*density,paint);
      String[] months={"Янв","Фев","Мар","Апр","Май","Июн","Июл","Авг","Сен","Окт","Ноя","Дек"};
      for(int m=0;m<12;m++){Calendar month=new GregorianCalendar(year,m,1);int index=(month.get(Calendar.DAY_OF_YEAR)-1+offset)/7;paint.setColor(MUTED);paint.setTextSize(10*density);canvas.drawText(months[m],left+index*gap,top-17*density,paint);}
      SimpleDateFormat fmt=new SimpleDateFormat("yyyy-MM-dd",Locale.US);Calendar today=Calendar.getInstance();
      int days=cal.getActualMaximum(Calendar.DAY_OF_YEAR);for(int day=0;day<days;day++){cal.set(Calendar.DAY_OF_YEAR,day+1);int col=(day+offset)/7,row=(day+offset)%7;float x=left+col*gap,y=top+row*gap;Float h=hours.get(fmt.format(cal.getTime()));int color=Color.rgb(23,35,31);
        if(h!=null){int bucket=h<6?0:h<7?1:h<8?2:h<9?3:4;color=palette[bucket];}
        if(cal.after(today))color=Color.rgb(14,23,20);
        paint.setColor(color);canvas.drawCircle(x,y,r,paint);
      }
    }
  }
  static class Graph extends View {JSONArray arr;Paint paint=new Paint(3);Graph(Context c,JSONArray a){super(c);arr=a;}protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();c.drawColor(BG);int n=Math.min(7,arr.length());if(n==0)return;float bar=w/(n*1.6f);for(int i=0;i<n;i++){JSONObject o=arr.optJSONObject(arr.length()-n+i);if(o==null)continue;float hours=(o.optLong("end")-o.optLong("start"))/3600000f;float bh=Math.min(h*.82f,Math.max(3,h*.82f*hours/12));float x=(i+.3f)*w/n;paint.setColor(ACCENT);c.drawRoundRect(x,h-bh-15,x+bar,h-15,12,12,paint);paint.setColor(TEXT);paint.setTextSize(25);c.drawText(String.valueOf(i+1),x+bar/3,h-1,paint);}}}
}
