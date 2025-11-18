package com.tuempresa.pathsecure.activities; // ¡Paquete cambiado!

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tuempresa.pathsecure.R;
// Importamos el modelo Route del mismo paquete
import com.tuempresa.pathsecure.activities.Route;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {

    private final List<Route> routesList;
    private final OnRouteActionListener listener;

    public void updateList(List<Route> routesList) {
    }



    public interface OnRouteActionListener {
        void onDeleteClick(Route route);
        void onItemClick(Route route);
    }

    public RouteAdapter(List<Route> routesList, OnRouteActionListener listener) {
        this.routesList = routesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Asegúrate de que el archivo item_route.xml esté creado en res/layout/
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        Route route = routesList.get(position);
        holder.bind(route, listener);
    }

    @Override
    public int getItemCount() {
        return routesList.size();
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder {
        final TextView txtRouteName;
        final TextView txtRouteDetails;
        final ImageButton btnDeleteRoute;

        RouteViewHolder(View itemView) {
            super(itemView);
            txtRouteName = itemView.findViewById(R.id.txtRouteName);
            txtRouteDetails = itemView.findViewById(R.id.txtRouteDetails);
            btnDeleteRoute = itemView.findViewById(R.id.btnDeleteRoute);
        }

        void bind(final Route route, final OnRouteActionListener listener) {
            txtRouteName.setText(route.getName());

            String details = route.getPoints().size() + " puntos registrados";
            if (route.getTimestamp() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
                details += " | Creada: " + sdf.format(route.getTimestamp());
            }
            txtRouteDetails.setText(details);

            btnDeleteRoute.setOnClickListener(v -> listener.onDeleteClick(route));
            itemView.setOnClickListener(v -> listener.onItemClick(route));
        }
    }
}