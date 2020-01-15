package mx.infornet.smartgym;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdapterMisRutinas extends RecyclerView.Adapter<AdapterMisRutinas.RutinaViewHolder> {

    private Context mContext;
    private List<Rutinas> rutinasList;

    public AdapterMisRutinas(Context mContext, List<Rutinas> rutinasList) {
        this.mContext = mContext;
        this.rutinasList = rutinasList;
    }

    @NonNull
    @Override
    public RutinaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.list_rutinas_layout, null);

        return new RutinaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterMisRutinas.RutinaViewHolder holder, int position) {
        Rutinas rutinas = rutinasList.get(position);

        holder.nombre.setText(rutinas.getNombre());
    }

    @Override
    public int getItemCount() {
        return rutinasList.size();
    }

    @Override
    public long getItemId(int position) {
        return rutinasList.get(position).getId();
    }

    class RutinaViewHolder extends RecyclerView.ViewHolder{
        TextView nombre;

        public RutinaViewHolder(View itemView){
            super(itemView);

            nombre = itemView.findViewById(R.id.nombre_rutina);

            CardView cardView = itemView.findViewById(R.id.card_view_item);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int requestCode = getAdapterPosition();
                    String nombreRutina = rutinasList.get(requestCode).getNombre();
                    int idRutina = rutinasList.get(requestCode).getId();
                    String descRutina = rutinasList.get(requestCode).getDescripcion();

                    Intent intent = new Intent(mContext, VerRutinaMiembroActivity.class);
                    intent.putExtra("id", idRutina);
                    intent.putExtra("nombre", nombreRutina);
                    intent.putExtra("descripcion", descRutina);

                    mContext.startActivity(intent);
                }
            });
        }
    }
}
