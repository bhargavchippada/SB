package sbpack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.sb.R;

public class LockPage extends Activity{
	public static String classname = "LockPage";

	EditText edtxt_password;
	Button btn_login;
	TextView txtvw_status;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//startActivity(new Intent(getBaseContext(),HomePage.class));
		setContentView(R.layout.layout_lock);

		edtxt_password = (EditText) findViewById(R.id.edtxt_password);
		btn_login = (Button) findViewById(R.id.btn_login);
		txtvw_status = (TextView) findViewById(R.id.txtvw_status);

		btn_login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String typed_password = edtxt_password.getText().toString();
				if(typed_password.equals("yo")){
					Intent intent = new Intent(getBaseContext(),HomePage.class);
					intent.putExtra("secure", false);
					startActivity(intent);
				}else if(typed_password.equals("secure")){
					Intent intent = new Intent(getBaseContext(),HomePage.class);
					intent.putExtra("secure", true);
					startActivity(intent);
				}else{
					txtvw_status.setText("o_O >_< :/");
				}
			}
		});
	}
}
