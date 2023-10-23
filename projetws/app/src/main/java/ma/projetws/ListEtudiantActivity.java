package ma.projetws;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ma.projetws.adapter.EtudiantAdapter;
import ma.projetws.beans.Etudiant;

public class ListEtudiantActivity extends AppCompatActivity {

    private ListView listView;
    private List<Etudiant> etudiantList;
    private EtudiantAdapter adapter;
    private String url = "http://192.168.181.73/Project/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_etudiant);
        listView = findViewById(R.id.listView);
        retrieveStudentsData();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Etudiant selectedEtudiant = etudiantList.get(position);

                new AlertDialog.Builder(ListEtudiantActivity.this)
                        .setTitle("Choisir une action")
                        .setItems(new CharSequence[]{"Modifier", "Supprimer"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    showEditDialog(selectedEtudiant);
                                } else {
                                    showDeleteConfirmation(selectedEtudiant);
                                }
                            }
                        })
                        .show();
            }
        });
    }

    private void retrieveStudentsData() {
        String loadUrl = this.url + "ws/loadEtudiant.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, loadUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Parse the JSON response and display data in ListView
                        Log.d("response", response+"");
                        handleJsonResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ListEtudiantActivity.this, "Erreur lors de la récupération des données", Toast.LENGTH_SHORT).show();
                    }
                });

        // Add the request to the RequestQueue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void handleJsonResponse(String response) {
        try {
            JSONArray jsonArray = new JSONArray(response);
            etudiantList = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Etudiant etudiant = new Etudiant(
                        jsonObject.getInt("id"),
                        jsonObject.getString("nom"),
                        jsonObject.getString("prenom"),
                        jsonObject.getString("ville"),
                        jsonObject.getString("sexe")
                );
                etudiantList.add(etudiant);
            }

            // Set up an adapter to display the list
            adapter = new EtudiantAdapter(this, etudiantList);
            listView.setAdapter((ListAdapter) adapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showEditDialog(final Etudiant etudiant) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modifier l'étudiant");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText nomInput = new EditText(this);
        nomInput.setHint("Nom");
        nomInput.setText(etudiant.getNom());
        layout.addView(nomInput);

        final EditText prenomInput = new EditText(this);
        prenomInput.setHint("Prénom");
        prenomInput.setText(etudiant.getPrenom());
        layout.addView(prenomInput);

        final Spinner villeSpinner = new Spinner(this);
        ArrayAdapter<CharSequence> adapterV = ArrayAdapter.createFromResource(this, R.array.villes, android.R.layout.simple_spinner_item);
        adapterV.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        villeSpinner.setAdapter(adapterV);
        villeSpinner.setSelection(adapterV.getPosition(etudiant.getVille()));
        layout.addView(villeSpinner);

        final RadioGroup sexeRadioGroup = new RadioGroup(this);

        RadioButton hommeRadio = new RadioButton(this);
        hommeRadio.setText("Homme");
        hommeRadio.setId(View.generateViewId());
        sexeRadioGroup.addView(hommeRadio);

        RadioButton femmeRadio = new RadioButton(this);
        femmeRadio.setText("Femme");
        femmeRadio.setId(View.generateViewId());
        sexeRadioGroup.addView(femmeRadio);

        if (etudiant.getSexe().equals("Homme")) {
            hommeRadio.setChecked(true);
        } else {
            femmeRadio.setChecked(true);
        }

        layout.addView(sexeRadioGroup);

        builder.setView(layout);

        builder.setPositiveButton("Enregistrer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newNom = nomInput.getText().toString();
                String newPrenom = prenomInput.getText().toString();
                String newVille = villeSpinner.getSelectedItem().toString();
                String newSexe = (hommeRadio.isChecked()) ? "Homme" : "Femme";

                Etudiant updatedEtudiant = new Etudiant(
                        etudiant.getId(),
                        newNom,
                        newPrenom,
                        newVille,
                        newSexe
                );

                int position = etudiantList.indexOf(etudiant);

                etudiantList.set(position, updatedEtudiant);

                adapter.notifyDataSetChanged();
                sendUpdateRequest(updatedEtudiant);
            }
        });

        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showDeleteConfirmation(final Etudiant etudiant) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Êtes-vous sûr de vouloir supprimer cet élève?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        etudiantList.remove(etudiant);
                        adapter.notifyDataSetChanged();
                        sendDeleteRequest(etudiant);
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void sendDeleteRequest(Etudiant etudiant) {
        String deleteUrl = url+"controller/deleteEtudiant.php?id=" + etudiant.getId();

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.DELETE, deleteUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error", error+"");
            }
        });

        requestQueue.add(request);
    }

    private void sendUpdateRequest(Etudiant etudiant){
        String updateUrl = url+"controller/updateEtudiant.php";
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest updateRequest = new StringRequest(Request.Method.POST, updateUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(), "Mise à jour réussie !", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("error", error+"");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(etudiant.getId()));
                params.put("nom", etudiant.getNom());
                params.put("prenom", etudiant.getPrenom());
                params.put("ville", etudiant.getVille());
                params.put("sexe", etudiant.getSexe());
                return params;
            }
        };

        requestQueue.add(updateRequest);
    }



}