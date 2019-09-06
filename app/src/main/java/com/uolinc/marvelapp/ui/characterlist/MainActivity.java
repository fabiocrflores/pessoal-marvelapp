package com.uolinc.marvelapp.ui.characterlist;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.uolinc.marvelapp.R;
import com.uolinc.marvelapp.model.Result;
import com.uolinc.marvelapp.ui.characterdetail.CharacterDetailActivity;

import java.util.ArrayList;

import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MainContrato.View {

    private ConstraintLayout constraintLayout;
    private Spinner spinnerOrderBy;
    private RecyclerView recyclerViewCharacter;
    private MainContrato.Presenter presenter;
    private ArrayList<Result> resultArrayList = new ArrayList<>();
    private ProgressBar progressBarList;
    private CharacterAdapter characterAdapter;
    private LinearLayoutManager linearLayoutManager;

    private int limit = 20, totalLoad = 20, offset = 0, total;
    private boolean isLoading = true, isLastPage = false;
    private String orderBy = "name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();

        spinnerOrderBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    orderBy = "name";
                }else{
                    orderBy = "modified";
                }
                resultArrayList.clear();
                progressBarList.setVisibility(View.VISIBLE);
                presenter.getData(20, 0, orderBy);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    /**
     * Referencia os objetos
     * Configura a toolbar
     * Instância a classe presenter
     * Chama método para setar os parâmetros da recyclerview
     * Chama método para setar os dados do spinner
     */
    @Override
    public void initialize() {
        constraintLayout = findViewById(R.id.constraintLayout);
        spinnerOrderBy = findViewById(R.id.spinnerOrderBy);
        recyclerViewCharacter = findViewById(R.id.recyclerViewCharacter);
        progressBarList = findViewById(R.id.progressBarList);
        ButterKnife.bind(this);
        presenter = new MainPresenter(this);
        setRecyclerViewCharacter();
        setSpinnerOrderBy();
    }

    /**
     * Seta parâmetros da recyclerview
     */
    @Override
    public void setRecyclerViewCharacter() {
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewCharacter.setLayoutManager(linearLayoutManager);
        recyclerViewCharacter.setHasFixedSize(true);
        characterAdapter = new CharacterAdapter(resultArrayList, this);
        recyclerViewCharacter.setAdapter(characterAdapter);
        recyclerViewCharacter.addOnScrollListener(recyclerViewOnScrollListener);
    }

    /**
     * Seta dados da spinner
     */
    @Override
    public void setSpinnerOrderBy() {
        ArrayAdapter<CharSequence> orderByAdapter;
        orderByAdapter = ArrayAdapter.createFromResource(this, R.array.order_by, R.layout.spinner_item);
        orderByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrderBy.setAdapter(orderByAdapter);
    }

    /**
     * Carrega a lista de personagens
     *
     * @param _resultArrayList lista de personagem
     */
    @Override
    public void loadCharacterList(ArrayList<Result> _resultArrayList) {
        runOnUiThread(() ->{
            resultArrayList.addAll(_resultArrayList);
            characterAdapter.notifyDataSetChanged();
            progressBarList.setVisibility(View.GONE);
            isLoading = false;
        });
    }

    /**
     * Abre a tela com os detalhes do personagem
     *
     * @param result retorno com os dados do webservice
     * @param urlImage url da imagem do personagem
     */
    @Override
    public void showCharacterDetailActivity(Result result, String urlImage) {
        Intent intent = new Intent(this, CharacterDetailActivity.class);
        intent.putExtra("name", result.getName());
        intent.putExtra("description", result.getDescription());
        intent.putExtra("urlImage", urlImage);

        Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle();
        startActivity(intent, bundle);
    }

    /**
     * Mostra mensagem de erro para o usuário com possibilidade de tentar carregar a lista novamente
     */
    @Override
    public void showError() {
        progressBarList.setVisibility(View.GONE);
        if (isLoading) {
            resultArrayList.add(new Result());
            int position = resultArrayList.size() - 1;
            resultArrayList.remove(position);
            characterAdapter.notifyItemRemoved(position);

            isLoading = false;
        }

        Snackbar snackbar = Snackbar.make(constraintLayout, getString(R.string.load_error), Snackbar.LENGTH_LONG);
        snackbar.setAction(getString(R.string.try_again), (View v) -> {
            progressBarList.setVisibility(View.VISIBLE);
            presenter.getData(limit, offset, "name");
            snackbar.dismiss();
        });

        View snackView = snackbar.getView();
        snackView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        TextView snackTextView = snackView.findViewById(com.google.android.material.R.id.snackbar_text);
        snackTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));
        TextView snackActionView = snackView.findViewById(com.google.android.material.R.id.snackbar_action);
        snackActionView.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.white));

        snackbar.show();
    }

    /**
     * Seta a quantidade total de personagens do webservice
     *
     * @param total quantidade total de personagens
     */
    @Override
    public void setTotal(int total) {
        this.total = total;
    }

    private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = linearLayoutManager.getChildCount();
            int totalItemCount = linearLayoutManager.getItemCount();
            int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

            if (!isLoading && !isLastPage) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= limit) {
                    loadMoreItems();
                }
            }
        }
    };

    /**
     * Faz a chamada para carregar mais 20 personagens quando chegar ao final da lista
     */
    private void loadMoreItems() {
        isLoading = true;
        if (total - totalLoad >=20) {
            totalLoad += 20;
        }else{
            totalLoad += (total - totalLoad);
            isLastPage = true;
        }
        offset += 20;
        presenter.getData(limit, offset, orderBy);
    }
}
