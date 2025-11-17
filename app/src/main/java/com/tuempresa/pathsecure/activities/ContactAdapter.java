package com.tuempresa.pathsecure.activities; // ¡Paquete cambiado!

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Importamos el modelo Contact del mismo paquete
import com.tuempresa.pathsecure.activities.Contact;
import com.tuempresa.pathsecure.R;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private final List<Contact> contactsList;
    private final OnContactActionListener listener;

    // Definición de la interfaz de acciones
    public interface OnContactActionListener {
        void onDeleteClick(Contact contact);
        void onItemClick(Contact contact); // Para editar
    }

    public ContactAdapter(List<Contact> contactsList, OnContactActionListener listener) {
        this.contactsList = contactsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Asegúrate de que el archivo item_contact.xml esté creado en res/layout/
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactsList.get(position);
        holder.bind(contact, listener);
    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    // Clase ViewHolder
    static class ContactViewHolder extends RecyclerView.ViewHolder {
        final TextView txtContactName;
        final TextView txtContactPhone;
        final ImageButton btnDeleteContact;

        ContactViewHolder(View itemView) {
            super(itemView);
            txtContactName = itemView.findViewById(R.id.txtContactName);
            txtContactPhone = itemView.findViewById(R.id.txtContactPhone);
            btnDeleteContact = itemView.findViewById(R.id.btnDeleteContact);
        }

        void bind(final Contact contact, final OnContactActionListener listener) {
            txtContactName.setText(contact.getName());
            txtContactPhone.setText(contact.getPhone());

            // Listener para el borrado
            btnDeleteContact.setOnClickListener(v -> listener.onDeleteClick(contact));

            // Listener para la edición (al hacer clic en el ítem)
            itemView.setOnClickListener(v -> listener.onItemClick(contact));
        }
    }
}